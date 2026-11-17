package com.ticketya.notificaciones.dtos;

import com.ticketya.notificaciones.entities.EstadoNotificacion;
import com.ticketya.notificaciones.entities.Notificacion;
import com.ticketya.notificaciones.entities.TipoNotificacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record NotificacionResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "COMPRA_CONFIRMADA") TipoNotificacion tipo,
        @Schema(example = "101", description = "ID de la compra o del evento, según el tipo") long referenciaId,
        @Schema(example = "0", description = "Versión usada para distinguir las modificaciones de un evento")
        long versionEvento,
        @Schema(example = "12") long clienteId,
        @Schema(example = "PENDIENTE") EstadoNotificacion estado,
        @Schema(example = "0") int intentos,
        Instant creadaEn,
        Instant proximoIntentoEn,
        Instant enviadaEn,
        String ultimoError
) {

    public static NotificacionResponseDTO desde(Notificacion notificacion) {
        return new NotificacionResponseDTO(notificacion.getId(), notificacion.getTipo(),
                notificacion.getReferenciaId(), notificacion.getVersionEvento(), notificacion.getClienteId(),
                notificacion.getEstado(), notificacion.getIntentos(), notificacion.getCreadaEn(),
                notificacion.getProximoIntentoEn(), notificacion.getEnviadaEn(), notificacion.getUltimoError());
    }
}
