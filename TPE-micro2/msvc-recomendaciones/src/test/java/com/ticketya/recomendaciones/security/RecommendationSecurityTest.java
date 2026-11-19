package com.ticketya.recomendaciones.security;

import com.ticketya.recomendaciones.controllers.*;
import com.ticketya.recomendaciones.services.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PreferenciasController.class, RecomendacionesController.class})
@Import({SecurityConfig.class, ClienteAccess.class})
class RecommendationSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean PreferenciasService preferences;
    @MockitoBean RecomendacionesService recommendations;
    @DynamicPropertySource static void key(DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
    }
    @Test void protectsBothApisAgainstMissingTokenAndForeignOwner() throws Exception {
        for (String path : List.of("/api/preferencias/8", "/api/recomendaciones/8")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).header("Authorization", "Bearer " + TestTokens.user(7, "CLIENTE")))
                .andExpect(status().isForbidden());
            mvc.perform(get(path).header("Authorization", "Bearer " + TestTokens.service("compras")))
                .andExpect(status().isForbidden());
        }
        verifyNoInteractions(preferences, recommendations);
    }
    @Test void rejectsExpiredMissingExpirationWrongSignatureAndUnknownRoles() throws Exception {
        var expired = TestTokens.claims("7", List.of("CLIENTE"), "user");
        expired.put("iat", java.time.Instant.now().minusSeconds(100)); expired.put("exp", java.time.Instant.now().minusSeconds(1));
        var missingExpiration = TestTokens.claims("7", List.of("CLIENTE"), "user"); missingExpiration.remove("exp");
        for (String token : List.of("altered", TestTokens.sign(expired, TestTokens.KEYS),
                TestTokens.sign(missingExpiration, TestTokens.KEYS), TestTokens.user(7, "UNKNOWN"),
                TestTokens.sign(TestTokens.claims("7", List.of("CLIENTE"), "user"), TestTokens.pair()))) {
            mvc.perform(get("/api/preferencias/7").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        }
    }
    @Test void acceptsOwnResourcesAndAdministrator() throws Exception {
        mvc.perform(get("/api/preferencias/7").header("Authorization", "Bearer " + TestTokens.user(7, "CLIENTE")))
            .andExpect(status().isOk());
        mvc.perform(get("/api/recomendaciones/8").header("Authorization", "Bearer " + TestTokens.user(7, "ADMINISTRADOR")))
            .andExpect(status().isOk());
        verify(preferences).consultar(7L); verify(recommendations).obtenerRecomendaciones(8L);
    }
}
