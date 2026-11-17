package com.ticketya.reservas.security;

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
import com.ticketya.reservas.controllers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ReservaController.class, DisponibilidadController.class})
@Import({SecurityConfig.class, ReservationAccess.class})
class ServiceSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean com.ticketya.reservas.services.ReservaService service;
    @MockitoBean com.ticketya.reservas.services.DisponibilidadService availability;
    @MockitoBean com.ticketya.reservas.repositories.ReservaRepository repository;
    @DynamicPropertySource static void key(DynamicPropertyRegistry r) {
        r.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
    }
    @BeforeEach void owners() {
        var entity = mock(com.ticketya.reservas.entities.Reserva.class);
        when(entity.getClienteId()).thenReturn(7L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        var foreign = mock(com.ticketya.reservas.entities.Reserva.class);
        when(foreign.getClienteId()).thenReturn(8L);
        when(repository.findById(2L)).thenReturn(Optional.of(foreign));
    }
    private String bearer(String identity) {
        return "Bearer " + (identity.startsWith("service:") ? TestTokens.service(identity.substring(8)) : TestTokens.user(7, identity));
    }
    static Stream<Arguments> forbiddenEndpoints() {
        List<String[]> endpoints = List.of(
            new String[]{"GET", "/reservas/1", "CLIENTE,ADMINISTRADOR,service:compras,service:entradas"},
            new String[]{"POST", "/reservas", "CLIENTE"},
            new String[]{"POST", "/reservas/1/confirmar", "service:compras"},
            new String[]{"POST", "/reservas/1/liberar", "service:compras"},
            new String[]{"POST", "/reservas/1/devolver-entrada", "service:entradas"},
            new String[]{"POST", "/reservas/evento/1/cancelar", "service:eventos"},
            new String[]{"POST", "/reservas/evento/1/habilitar", "service:eventos"});
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
        mvc.perform(get("/reservas/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/reservas/1").header("Authorization", "Bearer altered"))
            .andExpect(status().isUnauthorized());
    }
    @Test void rejectsForgedClientAndForeignReservation() throws Exception {
        mvc.perform(post("/reservas").header("Authorization", bearer("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"clienteId\":8,\"eventoId\":1}")).andExpect(status().isForbidden());
        mvc.perform(get("/reservas/2").header("Authorization", bearer("CLIENTE"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void acceptsOwnReservationAndInternalConfirmation() throws Exception {
        mvc.perform(get("/reservas/1").header("Authorization", bearer("CLIENTE"))).andExpect(status().isOk());
        mvc.perform(post("/reservas/1/confirmar").header("Authorization", bearer("service:compras"))).andExpect(status().isOk());
        verify(service).obtenerReserva(1L); verify(service).confirmarReserva(1L);
    }
    @Test void availabilityIsPublic() throws Exception {
        mvc.perform(get("/disponibilidad").param("eventoId", "1").param("sectorId", "1")).andExpect(status().isOk());
    }

    @Test void preservesForbiddenFromEventService() throws Exception {
        var failure = mock(feign.FeignException.Forbidden.class);
        when(failure.status()).thenReturn(403);
        when(availability.consultarDisponibilidad(1L, 1L)).thenThrow(failure);
        mvc.perform(get("/disponibilidad").param("eventoId", "1").param("sectorId", "1")).andExpect(status().isForbidden());
    }
}
