package ar.edu.unicen.ticketya.eventos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// El precio depende del evento y del sector, no solo del lugar.
@Entity
@Table(name="precios_sector", uniqueConstraints=@UniqueConstraint(columnNames={"evento_id", "sector_id"}))
public class PrecioSector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="evento_id", nullable=false)
    private Evento evento;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="sector_id", nullable=false)
    private Sector sector;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    protected PrecioSector() {}
    public PrecioSector(Evento evento, Sector sector, BigDecimal precio) {
        this.evento = evento;
        this.sector = sector;
        this.precio = precio;
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

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
