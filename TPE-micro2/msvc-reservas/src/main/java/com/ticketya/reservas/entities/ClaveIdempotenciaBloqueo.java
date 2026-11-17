package com.ticketya.reservas.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "clave_idempotencia_bloqueo")
public class ClaveIdempotenciaBloqueo {
    @Id
    @Column(name = "clave", length = 160)
    private String clave;

    public ClaveIdempotenciaBloqueo() { }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
