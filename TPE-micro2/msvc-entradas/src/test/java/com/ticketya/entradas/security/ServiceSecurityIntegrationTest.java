package com.ticketya.entradas.security;

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
import com.ticketya.entradas.controllers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EntradaController.class, DevolucionController.class})
@Import({SecurityConfig.class, EntryAccess.class})
class ServiceSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean com.ticketya.entradas.services.EntradaService service;
    @MockitoBean com.ticketya.entradas.services.DevolucionService returns;
    @MockitoBean com.ticketya.entradas.repositories.EntradaRepository repository;
    @DynamicPropertySource static void key(DynamicPropertyRegistry r) {
        r.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
    }
    @BeforeEach void owners() {
        var entity = mock(com.ticketya.entradas.entities.Entrada.class);
        when(entity.getClienteId()).thenReturn(7L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        var foreign = mock(com.ticketya.entradas.entities.Entrada.class);
        when(foreign.getClienteId()).thenReturn(8L);
        when(repository.findById(2L)).thenReturn(Optional.of(foreign));
    }
    private String bearer(String identity) {
        return "Bearer " + (identity.startsWith("service:") ? TestTokens.service(identity.substring(8)) : TestTokens.user(7, identity));
    }
    static Stream<Arguments> forbiddenEndpoints() {
        List<String[]> endpoints = List.of(
            new String[]{"POST", "/entradas/emitir", "service:compras"},
            new String[]{"GET", "/entradas/1", "CLIENTE,ADMINISTRADOR,service:compras"},
            new String[]{"GET", "/entradas/cliente/7", "CLIENTE,ADMINISTRADOR,service:compras"},
            new String[]{"POST", "/entradas/evento/1/invalidar", "service:eventos,service:compras"},
            new String[]{"POST", "/devoluciones", "CLIENTE"},
            new String[]{"GET", "/devoluciones/pendientes", "ADMINISTRADOR"},
            new String[]{"PATCH", "/devoluciones/1/aprobar", "ADMINISTRADOR"},
            new String[]{"PATCH", "/devoluciones/1/rechazar", "ADMINISTRADOR"});
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
        mvc.perform(get("/entradas/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/entradas/1").header("Authorization", "Bearer altered"))
            .andExpect(status().isUnauthorized());
    }
    @Test void rejectsForeignEntriesForgedHeaderAndForeignReturn() throws Exception {
        mvc.perform(get("/entradas/cliente/8").header("Authorization", bearer("CLIENTE"))).andExpect(status().isForbidden());
        mvc.perform(get("/entradas/2").header("X-Cliente-Id", "8").header("Authorization", bearer("CLIENTE"))).andExpect(status().isForbidden());
        mvc.perform(get("/entradas/2").header("X-Cliente-Id", "7").header("Authorization", bearer("CLIENTE"))).andExpect(status().isForbidden());
        mvc.perform(post("/devoluciones").header("Authorization", bearer("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"entradaId\":2,\"motivo\":\"No puedo asistir\"}")).andExpect(status().isForbidden());
        verifyNoInteractions(service, returns);
    }
    @Test void acceptsOwnEntriesAndOwnReturn() throws Exception {
        mvc.perform(get("/entradas/1").header("X-Cliente-Id", "7").header("Authorization", bearer("CLIENTE"))).andExpect(status().isOk());
        mvc.perform(post("/devoluciones").header("Authorization", bearer("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"entradaId\":1,\"motivo\":\"No puedo asistir\"}")).andExpect(status().isOk());
        verify(service).obtener(1L, 7L); verify(returns).solicitar(any());
    }
    @Test void administratorIdentityComesFromJwt() throws Exception {
        mvc.perform(patch("/devoluciones/1/aprobar").header("Authorization", bearer("ADMINISTRADOR"))).andExpect(status().isOk());
        verify(returns).aprobar(1L, 7L);
        mvc.perform(patch("/devoluciones/2/aprobar").param("administradorId", "99")
            .header("Authorization", bearer("ADMINISTRADOR"))).andExpect(status().isForbidden());
        verify(returns, never()).aprobar(2L, 99L);
    }
}
