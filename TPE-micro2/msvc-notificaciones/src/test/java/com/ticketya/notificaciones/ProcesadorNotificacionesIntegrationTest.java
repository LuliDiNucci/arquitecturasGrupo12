package com.ticketya.notificaciones;

import com.ticketya.notificaciones.dtos.CompraConfirmadaRequestDTO;
import com.ticketya.notificaciones.dtos.DestinatarioDTO;
import com.ticketya.notificaciones.dtos.NotificacionResponseDTO;
import com.ticketya.notificaciones.entities.EstadoNotificacion;
import com.ticketya.notificaciones.services.NotificacionService;
import com.ticketya.notificaciones.services.ProcesadorNotificaciones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "app.notificaciones.procesamiento-habilitado=false")
@ActiveProfiles("test")
class ProcesadorNotificacionesIntegrationTest {
    @org.springframework.test.context.DynamicPropertySource
    static void securityKey(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> com.ticketya.notificaciones.security.TestTokens.PUBLIC);
    }

    private static final Instant AHORA = Instant.parse("2026-11-16T22:00:00Z");
    private static final DestinatarioDTO CLIENTE = new DestinatarioDTO(12L, "cliente@example.test");

    @Autowired
    private NotificacionService service;
    @Autowired
    private ProcesadorNotificaciones procesador;
    @MockitoBean
    private JavaMailSender mailSender;
    @MockitoBean
    private Clock reloj;

    @BeforeEach
    void iniciarReloj() {
        avanzarReloj(0);
    }

    @Test
    void esperaElIntervaloYSeRecuperaDeUnFallo() {
        long id = registrarCompra(201L);
        verifyNoInteractions(mailSender);
        doThrow(new MailSendException("SMTP no responde")).doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

        procesador.procesar(id);
        assertThat(buscar(id).estado()).isEqualTo(EstadoNotificacion.PENDIENTE);
        assertThat(buscar(id).proximoIntentoEn()).isEqualTo(AHORA.plusSeconds(10));
        assertThat(buscar(id).ultimoError()).isEqualTo("MailSendException");

        avanzarReloj(9);
        procesador.procesar(id);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        avanzarReloj(10);
        procesador.procesar(id);
        assertThat(buscar(id).estado()).isEqualTo(EstadoNotificacion.ENVIADA);
        assertThat(buscar(id).intentos()).isEqualTo(2);
        assertThat(buscar(id).enviadaEn()).isEqualTo(AHORA.plusSeconds(10));
        assertThat(buscar(id).ultimoError()).isNull();
    }

    @Test
    void registraFallidaAlAgotarLosIntentos() {
        long id = registrarCompra(202L);
        doThrow(new MailSendException("SMTP no responde")).when(mailSender).send(any(SimpleMailMessage.class));

        procesador.procesar(id);
        avanzarReloj(10);
        procesador.procesar(id);
        avanzarReloj(20);
        procesador.procesar(id);
        avanzarReloj(100);
        procesador.procesar(id);

        assertThat(buscar(id).estado()).isEqualTo(EstadoNotificacion.FALLIDA);
        assertThat(buscar(id).intentos()).isEqualTo(3);
        assertThat(buscar(id).proximoIntentoEn()).isNull();
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void dosProcesadoresConcurrentesSoloEnvianUnaVez() throws Exception {
        long id = registrarCompra(203L);
        var envioIniciado = new CountDownLatch(1);
        var permitirEnvio = new CountDownLatch(1);
        var segundoProcesadorIniciado = new CountDownLatch(1);
        doAnswer(llamada -> {
            envioIniciado.countDown();
            assertThat(permitirEnvio.await(5, TimeUnit.SECONDS)).isTrue();
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));

        try (var executor = Executors.newFixedThreadPool(2)) {
            var primero = executor.submit(() -> procesador.procesar(id));
            try {
                assertThat(envioIniciado.await(5, TimeUnit.SECONDS)).isTrue();
                var segundo = executor.submit(() -> {
                    segundoProcesadorIniciado.countDown();
                    procesador.procesar(id);
                });
                assertThat(segundoProcesadorIniciado.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(buscar(id).estado()).isEqualTo(EstadoNotificacion.PENDIENTE);
                permitirEnvio.countDown();
                primero.get(5, TimeUnit.SECONDS);
                segundo.get(5, TimeUnit.SECONDS);
            } finally {
                permitirEnvio.countDown();
            }
        }

        assertThat(buscar(id).estado()).isEqualTo(EstadoNotificacion.ENVIADA);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    private long registrarCompra(long compraId) {
        return service.registrarCompraConfirmada(new CompraConfirmadaRequestDTO(CLIENTE, compraId, "Recital")).id();
    }

    private NotificacionResponseDTO buscar(long id) {
        return service.buscarPorId(id).orElseThrow();
    }

    private void avanzarReloj(long segundos) {
        when(reloj.instant()).thenReturn(AHORA.plusSeconds(segundos));
    }
}
