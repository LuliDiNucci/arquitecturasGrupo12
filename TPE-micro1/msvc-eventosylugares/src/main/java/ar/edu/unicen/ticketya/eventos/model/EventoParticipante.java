package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;

// Un evento puede tener varios participantes y un participante puede estar en varios eventos.
@Entity
@Table(name="eventos_participantes", uniqueConstraints=@UniqueConstraint(columnNames={"evento_id", "participante_id"}))
public class EventoParticipante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="evento_id", nullable=false)
    private Evento evento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="participante_id", nullable=false)
    private Participante participante;

    protected EventoParticipante() {}
    public EventoParticipante(Evento evento, Participante participante) {
        this.evento = evento;
        this.participante = participante;
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }
}
