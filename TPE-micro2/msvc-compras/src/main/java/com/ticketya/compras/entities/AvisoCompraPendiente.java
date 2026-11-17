package com.ticketya.compras.entities;

import jakarta.persistence.*;
import java.time.Instant;

/** Outbox local: una compra confirmada no depende de que Notificaciones esté disponible. */
@Entity
@Table(name = "avisos_compra_pendientes", uniqueConstraints =
        @UniqueConstraint(name = "uk_aviso_compra", columnNames = "compra_id"))
public class AvisoCompraPendiente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "compra_id", nullable = false)
    private Long compraId;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "nombre_evento", nullable = false, length = 180)
    private String nombreEvento;
    @Column(nullable = false, length = 16)
    private String estado;
    @Column(nullable = false)
    private int intentos;
    @Column(name = "proximo_intento_en", nullable = false)
    private Instant proximoIntentoEn;

    protected AvisoCompraPendiente() {}
    public AvisoCompraPendiente(Long compraId, Long clienteId, String nombreEvento, Instant ahora) {
        this.compraId = compraId; this.clienteId = clienteId; this.nombreEvento = nombreEvento;
        this.estado = "PENDIENTE"; this.proximoIntentoEn = ahora;
    }
    public void entregado() { estado = "ENTREGADO"; }
    public void reintentar(Instant cuando) { intentos++; proximoIntentoEn = cuando; }
    public Long getId() { return id; }
    public Long getCompraId() { return compraId; }
    public Long getClienteId() { return clienteId; }
    public String getNombreEvento() { return nombreEvento; }
    public String getEstado() { return estado; }
    public int getIntentos() { return intentos; }
    public Instant getProximoIntentoEn() { return proximoIntentoEn; }
}
