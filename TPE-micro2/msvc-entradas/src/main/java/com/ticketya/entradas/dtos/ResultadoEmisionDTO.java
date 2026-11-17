package com.ticketya.entradas.dtos;

import java.util.List;

/*
 * Clase auxiliar para devolver las entradas emitidas
 * e indicar si fueron creadas o ya existian.
 */
public class ResultadoEmisionDTO {

    private boolean creada;
    private List<EntradaDTO> entradas;

    public ResultadoEmisionDTO() {
    }

    public ResultadoEmisionDTO(
            boolean creada,
            List<EntradaDTO> entradas) {

        this.creada = creada;
        this.entradas = entradas;
    }

    public boolean isCreada() {
        return creada;
    }

    public void setCreada(boolean creada) {
        this.creada = creada;
    }

    public List<EntradaDTO> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<EntradaDTO> entradas) {
        this.entradas = entradas;
    }
}

