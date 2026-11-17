package com.ticketya.usuarios.dto;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String path;
    private final List<String> detalles;
    public ErrorResponse(int status, String error, String path, List<String> detalles) {
        this.status = status; this.error = error; this.path = path; this.detalles = detalles;
    }
    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getPath() { return path; }
    public List<String> getDetalles() { return detalles; }
}
