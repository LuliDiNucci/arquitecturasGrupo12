package com.ticketya.usuarios;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ticketya.usuarios.entities.*;
import com.ticketya.usuarios.repositories.UsuarioRepository;
import com.ticketya.usuarios.security.TestTokens;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtDecoder decoder;

    @DynamicPropertySource static void keys(DynamicPropertyRegistry r) {
        r.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
        r.add("ticketya.jwt.private-key", () -> TestTokens.PRIVATE);
    }
    private String email() { return UUID.randomUUID() + "@example.com"; }
    private String registerJson(String email) throws Exception {
        return json.writeValueAsString(Map.of("nombre", "Ana", "apellido", "Pérez", "email", email,
            "password", "Ejemplo-seguro-2026!", "roles", List.of("ADMINISTRADOR"), "rol", "ORGANIZADOR"));
    }
    private JsonNode register(String email) throws Exception {
        String response = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerJson(email)))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.roles[0]").value("CLIENTE")).andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("passwordHash", "Ejemplo-seguro");
        return json.readTree(response);
    }
    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email", email, "password", password))));
    }
    @Test void registerHashesNormalizesAndRejectsDuplicate() throws Exception {
        String email = email().toUpperCase(Locale.ROOT);
        register(" " + email + " ");
        Usuario user = repository.findByEmail(email.toLowerCase(Locale.ROOT)).orElseThrow();
        assertThat(user.getRoles()).containsExactly(Rol.CLIENTE);
        assertThat(user.getPasswordHash()).isNotEqualTo("Ejemplo-seguro-2026!");
        assertThat(passwords.matches("Ejemplo-seguro-2026!", user.getPasswordHash())).isTrue();
        assertThat(user.isActivo()).isTrue();
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerJson(email.toLowerCase(Locale.ROOT))))
            .andExpect(status().isConflict());
    }
    @ParameterizedTest @ValueSource(strings = {"email", "password", "nombre", "apellido", "unicode"})
    void rejectsInvalidRegistration(String field) throws Exception {
        ObjectNode body = (ObjectNode) json.readTree(registerJson(email()));
        body.put(field.equals("unicode") ? "password" : field,
            switch (field) { case "email" -> "inválido"; case "password" -> "corta"; case "unicode" -> "ñ".repeat(40); default -> " "; });
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
            .andExpect(status().isBadRequest());
    }
    @Test void loginIssuesRealJwtAndReadsOwnProfile() throws Exception {
        String email = email(); JsonNode user = register(email);
        String response = login(email.toUpperCase(Locale.ROOT), "Ejemplo-seguro-2026!").andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store")).andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(3600)).andReturn().getResponse().getContentAsString();
        String token = json.readTree(response).get("accessToken").asText();
        var jwt = decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo(user.get("id").asText());
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("CLIENTE");
        mvc.perform(get("/usuarios/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
    @Test void loginResponsesDoNotDistinguishWrongPasswordUnknownOrInactive() throws Exception {
        String email = email(); register(email);
        String wrong = login(email, "otra-contraseña").andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
        String unknown = login(email(), "otra-contraseña").andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
        Usuario user = repository.findByEmail(email).orElseThrow(); user.setActivo(false); repository.saveAndFlush(user);
        String inactive = login(email, "Ejemplo-seguro-2026!").andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(wrong).get("error")).isEqualTo(json.readTree(unknown).get("error"))
            .isEqualTo(json.readTree(inactive).get("error"));
    }
    @Test void limitsRepeatedLoginAttempts() throws Exception {
        String email = email();
        for (int i = 0; i < 5; i++) login(email, "incorrecta").andExpect(status().isUnauthorized());
        login(email, "incorrecta").andExpect(status().isTooManyRequests());
    }
    @Test void privateEndpointsRequireValidSignature() throws Exception {
        mvc.perform(get("/usuarios/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/usuarios/me").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
    }
    @Test void rolesAndOwnershipAreEnforced() throws Exception {
        String email = email(); long id = register(email).get("id").asLong();
        for (String role : List.of("CLIENTE", "ORGANIZADOR", "PERSONAL_ACCESO")) {
            mvc.perform(patch("/usuarios/{id}/roles", id).header("Authorization", "Bearer " + TestTokens.user(id, role))
                .contentType(MediaType.APPLICATION_JSON).content("{\"roles\":[\"ADMINISTRADOR\"]}"))
                .andExpect(status().isForbidden());
        }
        mvc.perform(get("/usuarios/{id}", id).header("Authorization", "Bearer " + TestTokens.user(id + 1, "CLIENTE")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios/{id}", id).header("Authorization", "Bearer " + TestTokens.user(id, "CLIENTE")))
            .andExpect(status().isOk());
        mvc.perform(patch("/usuarios/{id}/roles", id).header("Authorization", "Bearer " + TestTokens.user(1000, "ADMINISTRADOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"roles\":[\"CLIENTE\",\"ORGANIZADOR\"]}"))
            .andExpect(status().isOk());
        assertThat(repository.findById(id).orElseThrow().getRoles()).containsExactlyInAnyOrder(Rol.CLIENTE, Rol.ORGANIZADOR);
        mvc.perform(patch("/usuarios/{id}/estado", id).header("Authorization", "Bearer " + TestTokens.user(1000, "ADMINISTRADOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}")).andExpect(status().isOk());
        login(email, "Ejemplo-seguro-2026!").andExpect(status().isUnauthorized());
    }
    @Test void serviceCredentialsIssueRestrictedToken() throws Exception {
        mvc.perform(post("/auth/service-token")).andExpect(status().isUnauthorized());
        String basic = Base64.getEncoder().encodeToString("compras:test-only-service-secret-with-at-least-32-characters".getBytes());
        String body = mvc.perform(post("/auth/service-token").header("Authorization", "Basic " + basic))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var jwt = decoder.decode(json.readTree(body).get("accessToken").asText());
        assertThat(jwt.getSubject()).isEqualTo("service:compras");
        assertThat(jwt.getClaimAsStringList("roles")).isEmpty();
        mvc.perform(get("/usuarios/me").header("Authorization", "Bearer " + jwt.getTokenValue())).andExpect(status().isForbidden());
    }
    @Test void administratorCanReadCurrentIdentityForOrganizerLinkWithoutGrantingServiceAccess() throws Exception {
        Usuario user = repository.saveAndFlush(new Usuario("Ana", "Perez", email(),
            passwords.encode("Ejemplo-seguro-2026!"), Set.of(Rol.ORGANIZADOR)));
        String admin = "Bearer " + TestTokens.user(1000, "ADMINISTRADOR");
        mvc.perform(get("/usuarios/{id}", user.getId()).header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.activo").value(true))
            .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("ORGANIZADOR")))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
        user.setActivo(false);
        repository.saveAndFlush(user);
        mvc.perform(get("/usuarios/{id}", user.getId()).header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get("/usuarios/{id}", user.getId())
                .header("Authorization", "Bearer " + TestTokens.service("eventos")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios/{id}", user.getId() + 10000).header("Authorization", admin))
            .andExpect(status().isNotFound());
    }

    @Test void openApiDocumentsBearerAndPublicOperations() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andExpect(jsonPath("$.components.securitySchemes.bearerJwt.scheme").value("bearer"))
            .andExpect(jsonPath("$.paths['/auth/register'].post.responses['201']").exists())
            .andExpect(jsonPath("$.paths['/usuarios/{id}/roles'].patch").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
