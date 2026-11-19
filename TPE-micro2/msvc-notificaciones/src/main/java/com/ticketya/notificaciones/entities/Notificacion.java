package com.ticketya.notificaciones.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "notificaciones", uniqueConstraints = {
        @UniqueConstraint(name = "uk_notificacion_destinatario", columnNames = {
                "tipo", "referencia_id", "version_evento", "cliente_id"
        })
}, indexes = {
        @Index(name = "idx_notificacion_envio", columnList = "estado, proximo_intento_en, id")
})
public class Notificacion {
    // Nullable para registros anteriores; los nuevos capturan el contexto actual.
    @Column(name = "correlation_id", length = 64)
    private String correlationId = com.ticketya.observability.Correlation.currentOrNew();

    public String getCorrelationId() {
        correlationId = com.ticketya.observability.Correlation.validateOrNew(correlationId);
        return correlationId;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(name = "referencia_id", nullable = false)
    private long referenciaId;

    @Column(name = "version_evento", nullable = false)
    private long versionEvento;

    @Column(name = "cliente_id", nullable = false)
    private long clienteId;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 120)
    private String asunto;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoNotificacion estado;

    @Column(nullable = false)
    private int intentos;

    @Column(nullable = false)
    private Instant creadaEn;

    @Column(name = "proximo_intento_en")
    private Instant proximoIntentoEn;

    private Instant enviadaEn;

    @Column(length = 80)
    private String ultimoError;

    protected Notificacion() {}

    public Notificacion(TipoNotificacion tipo, long referenciaId, long versionEvento,
                         long clienteId, String email, String asunto, String mensaje, Instant ahora) {
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.versionEvento = versionEvento;
        this.clienteId = clienteId;
        this.email = email;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
        this.creadaEn = ahora;
        this.proximoIntentoEn = ahora;
    }

    public boolean listaParaEnviar(Instant ahora) {
        return estado == EstadoNotificacion.PENDIENTE && !proximoIntentoEn.isAfter(ahora);
    }

    public void registrarEnvio(Instant ahora) {
        intentos++;
        estado = EstadoNotificacion.ENVIADA;
        enviadaEn = ahora;
        proximoIntentoEn = null;
        ultimoError = null;
    }

    public void registrarFallo(String error, Instant ahora, int maxIntentos, long reintentoSegundos) {
        intentos++;
        ultimoError = error;
        if (intentos >= maxIntentos) {
            estado = EstadoNotificacion.FALLIDA;
            proximoIntentoEn = null;
        } else {
            proximoIntentoEn = ahora.plusSeconds(reintentoSegundos);
        }
    }

    public Long getId() { return id; }
    public TipoNotificacion getTipo() { return tipo; }
    public long getReferenciaId() { return referenciaId; }
    public long getVersionEvento() { return versionEvento; }
    public long getClienteId() { return clienteId; }
    public String getEmail() { return email; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }
    public EstadoNotificacion getEstado() { return estado; }
    public int getIntentos() { return intentos; }
    public Instant getCreadaEn() { return creadaEn; }
    public Instant getProximoIntentoEn() { return proximoIntentoEn; }
    public Instant getEnviadaEn() { return enviadaEn; }
    public String getUltimoError() { return ultimoError; }
}
