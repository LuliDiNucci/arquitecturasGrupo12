package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "avisos_clientes_evento",
        uniqueConstraints = @UniqueConstraint(name = "uk_aviso_cliente_evento", columnNames = {
                "evento_id", "cancelacion", "version_evento"
        }),
        indexes = @Index(name = "idx_aviso_cliente_evento_pendiente", columnList = "entregado, intentar_en"))
public class AvisoEventoPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;
    @Column(name = "version_evento", nullable = false)
    private Long versionEvento;
    @Column(nullable = false, length = 180)
    private String nombreEvento;
    @Column(length = 1000)
    private String detalleCambios;
    @Column(nullable = false)
    private boolean cancelacion;
    @Column(nullable = false)
    private boolean entregado;
    @Column(name = "intentar_en", nullable = false)
    private Instant intentarEn;

    protected AvisoEventoPendiente() {}

    private AvisoEventoPendiente(Long eventoId, Long versionEvento, String nombreEvento,
            String detalleCambios, boolean cancelacion, Instant ahora) {
        this.eventoId = eventoId;
        this.versionEvento = versionEvento;
        this.nombreEvento = nombreEvento;
        this.detalleCambios = detalleCambios;
        this.cancelacion = cancelacion;
        this.intentarEn = ahora;
    }

    public static AvisoEventoPendiente cancelacion(Long eventoId, String nombreEvento, Instant ahora) {
        return new AvisoEventoPendiente(eventoId, 0L, nombreEvento, null, true, ahora);
    }

    public static AvisoEventoPendiente modificacion(Long eventoId, Long versionEvento, String nombreEvento,
            String detalleCambios, Instant ahora) {
        return new AvisoEventoPendiente(eventoId, versionEvento, nombreEvento, detalleCambios, false, ahora);
    }

    public Long getEventoId() { return eventoId; }
    public Long getVersionEvento() { return versionEvento; }
    public String getNombreEvento() { return nombreEvento; }
    public String getDetalleCambios() { return detalleCambios; }
    public boolean isCancelacion() { return cancelacion; }

    public boolean estaPendiente(Instant ahora) {
        return !entregado && !intentarEn.isAfter(ahora);
    }

    public void marcarEntregado() {
        entregado = true;
    }

    public void reintentar(Instant cuando) {
        intentarEn = cuando;
    }
}
