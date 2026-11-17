package com.ticketya.notificaciones.services;

import com.ticketya.notificaciones.config.NotificacionesProperties;
import com.ticketya.notificaciones.entities.Notificacion;
import com.ticketya.notificaciones.repositories.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ProcesadorNotificaciones {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorNotificaciones.class);

    private final NotificacionRepository repository;
    private final JavaMailSender mailSender;
    private final NotificacionesProperties configuracion;
    private final Clock reloj;

    public ProcesadorNotificaciones(NotificacionRepository repository, JavaMailSender mailSender,
                                    NotificacionesProperties configuracion, Clock reloj) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    @Transactional
    public void procesar(long id) {
        // El bloqueo dura un envío y evita que otra instancia procese la misma fila en paralelo.
        repository.buscarParaProcesar(id)
                .filter(notificacion -> notificacion.listaParaEnviar(reloj.instant()))
                .ifPresent(this::enviar);
    }

    private void enviar(Notificacion notificacion) {
        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setFrom(configuracion.remitente());
        correo.setTo(notificacion.getEmail());
        correo.setSubject(notificacion.getAsunto());
        correo.setText(notificacion.getMensaje());

        try {
            mailSender.send(correo);
            notificacion.registrarEnvio(reloj.instant());
        } catch (MailException ex) {
            notificacion.registrarFallo(ex.getClass().getSimpleName(), reloj.instant(),
                    configuracion.maxIntentos(), configuracion.reintentoSegundos());
            log.warn("Falló el intento {} de la notificación {}. Estado: {}. Error: {}",
                    notificacion.getIntentos(), notificacion.getId(),
                    notificacion.getEstado(), notificacion.getUltimoError());
        }
    }
}
