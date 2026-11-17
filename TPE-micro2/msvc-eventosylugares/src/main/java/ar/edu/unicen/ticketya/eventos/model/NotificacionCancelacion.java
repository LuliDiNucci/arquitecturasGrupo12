package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="notificaciones_cancelacion")
public class NotificacionCancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long eventoId;
    @Column(nullable = false)
    private boolean reservasNotificadas;
    @Column(nullable = false)
    private boolean entradasNotificadas;
    // Boolean permite leer registros anteriores donde esta columna todavia no existia.
    private Boolean comprasNotificadas = false;
    @Column(nullable = false)
    private int intentos;
    @Column(nullable = false)
    private LocalDateTime creadaEn;
    private LocalDateTime ultimoIntentoEn;
    @Column(length = 1200)
    private String ultimoError;

    protected NotificacionCancelacion() {}
    public NotificacionCancelacion(Long eventoId, boolean reservasNotificadas, boolean entradasNotificadas, int intentos, LocalDateTime creadaEn, LocalDateTime ultimoIntentoEn, String ultimoError) {
        this.eventoId = eventoId;
        this.reservasNotificadas = reservasNotificadas;
        this.entradasNotificadas = entradasNotificadas;
        this.intentos = intentos;
        this.creadaEn = creadaEn;
        this.ultimoIntentoEn = ultimoIntentoEn;
        this.ultimoError = ultimoError;
    }

    public Long getId() {
        return id;
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
        return Boolean.TRUE.equals(comprasNotificadas);
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

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(LocalDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }

    public LocalDateTime getUltimoIntentoEn() {
        return ultimoIntentoEn;
    }

    public void setUltimoIntentoEn(LocalDateTime ultimoIntentoEn) {
        this.ultimoIntentoEn = ultimoIntentoEn;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }
}
