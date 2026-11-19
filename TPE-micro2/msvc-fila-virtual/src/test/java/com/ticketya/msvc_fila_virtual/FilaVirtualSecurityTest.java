package com.ticketya.msvc_fila_virtual;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketya.msvc_fila_virtual.controllers.FilaVirtualController;
import com.ticketya.msvc_fila_virtual.security.SecurityConfig;
import com.ticketya.msvc_fila_virtual.services.FilaVirtualService;

@WebMvcTest(FilaVirtualController.class)
@Import(SecurityConfig.class)
class FilaVirtualSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FilaVirtualService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void sinJwtNoPuedeIngresar() throws Exception {

        mvc.perform(post("/filas/1/ingresar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":10}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void clienteNoPuedeIngresarEnNombreDeOtro() throws Exception {

        mvc.perform(post("/filas/1/ingresar")
                .with(jwt()
                        .jwt(token -> token.subject("10"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":25}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void clientePuedeConsultarSuPosicion() throws Exception {

        mvc.perform(get("/filas/1/clientes/10")
                .with(jwt()
                        .jwt(token -> token.subject("10"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isOk());

        verify(service).consultarEstado(1L, 10L);
    }

    @Test
    void clienteNoPuedeConsultarPosicionAjena() throws Exception {

        mvc.perform(get("/filas/1/clientes/25")
                .with(jwt()
                        .jwt(token -> token.subject("10"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void clienteNoPuedeHabilitarTandas() throws Exception {

        mvc.perform(post("/filas/1/habilitar")
                .with(jwt()
                        .jwt(token -> token.subject("10"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":3}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void administradorPuedeHabilitarTandas() throws Exception {

        mvc.perform(post("/filas/1/habilitar")
                .with(jwt()
                        .jwt(token -> token.subject("5"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":3}"))
                .andExpect(status().isOk());

        verify(service).habilitarTanda(1L, 3);
    }

    @Test
    void clienteNoPuedeFinalizarTurno() throws Exception {

        mvc.perform(post("/filas/1/clientes/10/finalizar")
                .with(jwt()
                        .jwt(token -> token.subject("10"))
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void servicioReservasPuedeFinalizarTurno() throws Exception {

        mvc.perform(post("/filas/1/clientes/10/finalizar")
                .with(jwt()
                        .jwt(token -> token.subject("service:reservas"))
                        .authorities(
                                new SimpleGrantedAuthority("SERVICE_reservas"))))
                .andExpect(status().isOk());

        verify(service).finalizarTurno(1L, 10L);
    }
}