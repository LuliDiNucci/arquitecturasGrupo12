package com.ticketya.compras.security;

import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.ticketya.compras.controllers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CompraController.class, PagoController.class, ReintegroController.class})
@Import({SecurityConfig.class, PurchaseAccess.class})
class ServiceSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean com.ticketya.compras.services.CompraService service;
    @MockitoBean com.ticketya.compras.services.PagoService payments;
    @MockitoBean com.ticketya.compras.services.ReintegroService refunds;
    @MockitoBean com.ticketya.compras.repositories.CompraRepository repository;
    @DynamicPropertySource static void key(DynamicPropertyRegistry r) {
        r.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
    }
    @BeforeEach void owners() {
        var entity = mock(com.ticketya.compras.entities.Compra.class);
        when(entity.getClienteId()).thenReturn(7L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        var foreign = mock(com.ticketya.compras.entities.Compra.class);
        when(foreign.getClienteId()).thenReturn(8L);
        when(repository.findById(2L)).thenReturn(Optional.of(foreign));
    }
    private String bearer(String identity) {
        return "Bearer " + (identity.startsWith("service:") ? TestTokens.service(identity.substring(8)) : TestTokens.user(7, identity));
    }
    static Stream<Arguments> forbiddenEndpoints() {
        List<String[]> endpoints = List.of(
            new String[]{"POST", "/compras", "CLIENTE"},
            new String[]{"GET", "/compras/1", "CLIENTE,ADMINISTRADOR"},
            new String[]{"GET", "/compras/1/confirmacion", "CLIENTE,ADMINISTRADOR,service:entradas"},
            new String[]{"POST", "/compras/1/confirmar", "CLIENTE"},
            new String[]{"POST", "/compras/1/pagar", "CLIENTE"},
            new String[]{"POST", "/compras/1/reintentar-liberacion", "CLIENTE"},
            new String[]{"POST", "/compras/reintegros", "service:entradas"},
            new String[]{"GET", "/compras/1/reintegros", "CLIENTE,ADMINISTRADOR"},
            new String[]{"POST", "/compras/evento/1/cancelar", "service:eventos"},
            new String[]{"POST", "/compras/1/compensar", "ADMINISTRADOR"});
        return endpoints.stream().flatMap(endpoint -> Stream.of("CLIENTE", "ORGANIZADOR", "ADMINISTRADOR", "PERSONAL_ACCESO",
            "service:compras", "service:entradas", "service:eventos", "service:reservas")
            .filter(role -> !Arrays.asList(endpoint[2].split(",")).contains(role))
            .map(role -> Arguments.of(endpoint[0], endpoint[1], role)));
    }
    @ParameterizedTest @MethodSource("forbiddenEndpoints")
    void rejectsUnprivilegedRoleForEachEndpoint(String method, String path, String role) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path).header("Authorization", bearer(role))
            .header("X-Cliente-Id", "7").header("X-Operacion-Id", "test-operation")
            .param("administradorId", "7").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }
    @Test void directRequestsRequireCryptographicallyValidToken() throws Exception {
        mvc.perform(get("/compras/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/compras/1").header("Authorization", "Bearer altered"))
            .andExpect(status().isUnauthorized());
    }
    @Test void rejectsForgedClientAndForeignPurchaseIncludingPayments() throws Exception {
        mvc.perform(post("/compras").header("Authorization", bearer("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"clienteId\":8,\"reservaId\":1}")).andExpect(status().isForbidden());
        mvc.perform(get("/compras/2").header("Authorization", bearer("CLIENTE"))).andExpect(status().isForbidden());
        mvc.perform(post("/compras/2/pagar").header("Authorization", bearer("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
            .content("{}")).andExpect(status().isForbidden());
        mvc.perform(get("/compras/2/reintegros").header("Authorization", bearer("CLIENTE"))).andExpect(status().isForbidden());
        verifyNoInteractions(service, payments, refunds);
    }
    @Test void acceptsOwnPurchaseAndRestrictedServiceRead() throws Exception {
        mvc.perform(get("/compras/1").header("Authorization", bearer("CLIENTE"))).andExpect(status().isOk());
        mvc.perform(get("/compras/2/confirmacion").header("Authorization", bearer("service:entradas"))).andExpect(status().isOk());
        mvc.perform(post("/compras/1/compensar").header("Authorization", bearer("ADMINISTRADOR"))).andExpect(status().isNoContent());
        verify(service).obtenerCompra(1L); verify(service).consultarConfirmacion(2L);
        verify(refunds).compensarCompraSinEntradas(1L);
    }

    @Test void preservesForbiddenFromReservationService() throws Exception {
        var failure = mock(feign.FeignException.Forbidden.class);
        when(failure.status()).thenReturn(403);
        when(service.iniciarCompra(any())).thenThrow(failure);
        mvc.perform(post("/compras").header("Authorization", bearer("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"clienteId\":7,\"reservaId\":2}")).andExpect(status().isForbidden());
    }
}
