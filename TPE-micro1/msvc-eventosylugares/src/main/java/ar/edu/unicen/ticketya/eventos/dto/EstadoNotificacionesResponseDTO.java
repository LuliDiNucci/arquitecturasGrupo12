package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class EstadoNotificacionesResponseDTO {
    @Schema(example = "1")
    private Long eventoId;
    @Schema(example = "false")
    private boolean reservasNotificadas;
    @Schema(example = "false")
    private boolean entradasNotificadas;
    @Schema(example = "false")
    private boolean comprasNotificadas;
    @Schema(example = "0")
    private int intentos;
    private String ultimoError;

    public EstadoNotificacionesResponseDTO() {
    }

    public EstadoNotificacionesResponseDTO(Long eventoId, boolean reservasNotificadas, boolean entradasNotificadas, boolean comprasNotificadas, int intentos, String ultimoError) {
        this.eventoId = eventoId;
        this.reservasNotificadas = reservasNotificadas;
        this.entradasNotificadas = entradasNotificadas;
        this.comprasNotificadas = comprasNotificadas;
        this.intentos = intentos;
        this.ultimoError = ultimoError;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public boolean isReservasNotificadas() {
        return reservasNotificadas;
    }

    public void setReservasNotificadas(boolean reservasNotificadas) {
        this.reservasNotificadas = reservasNotificadas;
    }

    public boolean isEntradasNotificadas() {
        return entradasNotificadas;
    }

    public void setEntradasNotificadas(boolean entradasNotificadas) {
        this.entradasNotificadas = entradasNotificadas;
    }

    public boolean isComprasNotificadas() {
        return comprasNotificadas;
    }

    public void setComprasNotificadas(boolean comprasNotificadas) {
        this.comprasNotificadas = comprasNotificadas;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }
}
