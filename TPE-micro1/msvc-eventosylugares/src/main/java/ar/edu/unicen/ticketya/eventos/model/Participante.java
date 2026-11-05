package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "participantes")
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private TipoParticipante tipo;

    protected Participante() {}
    public Participante(String nombre, TipoParticipante tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public TipoParticipante getTipo() {
        return tipo;
    }

    public void setTipo(TipoParticipante tipo) {
        this.tipo = tipo;
    }
}
