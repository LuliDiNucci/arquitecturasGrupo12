package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "asientos", uniqueConstraints = @UniqueConstraint(columnNames={"sector_id", "fila", "numero"}))
public class Asiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="sector_id", nullable=false)
    private Sector sector;
    @Column(nullable = false, length = 20)
    private String fila;
    @Column(nullable = false)
    private int numero;

    protected Asiento() {}
    public Asiento(Sector sector, String fila, int numero) {
        this.sector = sector;
        this.fila = fila;
        this.numero = numero;
    }

    public Long getId() {
        return id;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
