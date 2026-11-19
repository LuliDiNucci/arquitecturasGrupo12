package com.ticketya.notificaciones.security;

import com.ticketya.notificaciones.dtos.*;
import com.ticketya.notificaciones.services.NotificacionService;
import com.ticketya.notificaciones.services.ProcesadorNotificaciones;
import com.ticketya.observability.Correlation;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.notificaciones.procesamiento-habilitado=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.extension.ExtendWith(org.springframework.boot.test.system.OutputCaptureExtension.class)
class NotificationSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired NotificacionService service;
    @Autowired ProcesadorNotificaciones processor;
    @Autowired com.ticketya.notificaciones.repositories.NotificacionRepository repository;
    @MockitoBean JavaMailSender mail;
    @DynamicPropertySource static void key(DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
    }
    private String client(long id) { return "Bearer " + TestTokens.user(id, "CLIENTE"); }
    private long purchase() {
        return service.registrarCompraConfirmada(new CompraConfirmadaRequestDTO(
            new DestinatarioDTO(7L, "private@example.test"), 100L, "Recital")).id();
    }
    @ParameterizedTest @ValueSource(strings = {"compras-confirmadas", "eventos-cancelados", "eventos-modificados", "artistas-favoritos"})
    void internalEndpointsNeverAcceptAnonymousOrHumanTokens(String operation) throws Exception {
        mvc.perform(post("/notificaciones/" + operation).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized()).andExpect(header().exists(Correlation.HEADER));
        for (String role : List.of("CLIENTE", "ORGANIZADOR", "PERSONAL_ACCESO", "ADMINISTRADOR")) {
            mvc.perform(post("/notificaciones/" + operation).header("Authorization", "Bearer " + TestTokens.user(7, role))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        }
    }
    @Test void clientsReadOnlyOwnNotificationsAndCannotForgeIdentity() throws Exception {
        long id = purchase();
        mvc.perform(get("/notificaciones/{id}", id)).andExpect(status().isUnauthorized());
        mvc.perform(get("/notificaciones/{id}", id).header("Authorization", client(8))
            .header("X-Cliente-Id", "7").header(Correlation.HEADER, "foreign-notification"))
            .andExpect(status().isForbidden()).andExpect(header().string(Correlation.HEADER, "foreign-notification"));
        mvc.perform(get("/notificaciones/{id}", id).header("Authorization", client(7)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.clienteId").value(7)).andExpect(jsonPath("$.email").doesNotExist());
        mvc.perform(get("/notificaciones/{id}", id).header("Authorization", "Bearer " + TestTokens.user(9, "ADMINISTRADOR")))
            .andExpect(status().isOk());
    }
    @Test void serviceCredentialsHaveOnlyTheirOwnOperationsAndNotificationTypes() throws Exception {
        String body = """
            {"destinatario":{"clienteId":7,"email":"private@example.test"},"compraId":100,"nombreEvento":"Recital"}
            """;
        mvc.perform(post("/notificaciones/compras-confirmadas").header("Authorization", "Bearer " + TestTokens.service("compras"))
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isAccepted());
        mvc.perform(post("/notificaciones/compras-confirmadas").header("Authorization", "Bearer " + TestTokens.service("eventos"))
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/notificaciones/eventos-cancelados").header("Authorization", "Bearer " + TestTokens.service("compras"))
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        long id = purchase();
        mvc.perform(get("/notificaciones/{id}", id).header("Authorization", "Bearer " + TestTokens.service("compras"))).andExpect(status().isOk());
        mvc.perform(get("/notificaciones/{id}", id).header("Authorization", "Bearer " + TestTokens.service("eventos"))).andExpect(status().isForbidden());
    }
    static Stream<Arguments> invalidTokens() {
        var expired = TestTokens.claims("7", List.of("CLIENTE"), "user");
        expired.put("iat", Instant.now().minusSeconds(100)); expired.put("exp", Instant.now().minusSeconds(2));
        var audience = TestTokens.claims("7", List.of("CLIENTE"), "user"); audience.put("aud", List.of("another-api"));
        var issuer = TestTokens.claims("7", List.of("CLIENTE"), "user"); issuer.put("iss", "another-issuer");
        var noExpiration = TestTokens.claims("7", List.of("CLIENTE"), "user"); noExpiration.remove("exp");
        var invalidRole = TestTokens.claims("7", List.of("ROOT"), "user");
        return Stream.of("altered", TestTokens.sign(expired, TestTokens.KEYS), TestTokens.sign(audience, TestTokens.KEYS),
            TestTokens.sign(issuer, TestTokens.KEYS), TestTokens.sign(noExpiration, TestTokens.KEYS),
            TestTokens.sign(invalidRole, TestTokens.KEYS), TestTokens.sign(TestTokens.claims("7", List.of("CLIENTE"), "user"), TestTokens.pair()))
            .map(Arguments::of);
    }
    @ParameterizedTest @MethodSource("invalidTokens")
    void rejectsInvalidSignedClaimsExpiredTokensAndWrongSignature(String token) throws Exception {
        mvc.perform(get("/notificaciones/1").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized()).andExpect(header().exists(Correlation.HEADER));
    }
    @Test void persistsCorrelationAndRestoresItDuringActualBackgroundDelivery() {
        long id;
        try (var scope = Correlation.open("purchase-outbox-42")) { id = purchase(); }
        assertThat(repository.findById(id).orElseThrow().getCorrelationId()).isEqualTo("purchase-outbox-42");
        doAnswer(invocation -> {
            assertThat(org.slf4j.MDC.get(Correlation.KEY)).isEqualTo("purchase-outbox-42");
            return null;
        }).when(mail).send(any(org.springframework.mail.SimpleMailMessage.class));
        processor.procesar(id);
        verify(mail).send(any(org.springframework.mail.SimpleMailMessage.class));
        assertThat(org.slf4j.MDC.get(Correlation.KEY)).isNull();
    }
    @Test void emitsRealStructuredJsonIncludingHandledErrorsWithoutSensitiveFields(
            org.springframework.boot.test.system.CapturedOutput output) throws Exception {
        mvc.perform(get("/notificaciones/{id}", purchase()).header("Authorization", client(8))
            .header(Correlation.HEADER, "json-format-evidence").header("Cookie", "session=NEVER_LOG_THIS"))
            .andExpect(status().isForbidden());
        String line = output.getOut().lines().filter(value -> value.contains("\"evento\":\"http_request\"")
            && value.contains("json-format-evidence")).findFirst().orElseThrow();
        var json = new tools.jackson.databind.json.JsonMapper().readTree(line);
        assertThat(json.path("servicio").asString()).isEqualTo("msvc-notificaciones");
        assertThat(json.path("usuario").asString()).isEqualTo("8");
        assertThat(json.path("status").asInt()).isEqualTo(403);
        assertThat(json.path("duracionMs").isNumber()).isTrue();
        assertThat(json.path("@timestamp").asString()).isNotBlank();
        assertThat(line).doesNotContain("NEVER_LOG_THIS", "Bearer ");
    }
}
