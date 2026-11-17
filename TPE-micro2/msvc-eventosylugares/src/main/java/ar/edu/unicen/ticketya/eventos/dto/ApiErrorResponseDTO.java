package ar.edu.unicen.ticketya.eventos.dto;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error de la API")
public class ApiErrorResponseDTO {

    private LocalDateTime timestamp;
    @Schema(example = "409")
    private int status;
    @Schema(example = "Conflict")
    private String error;
    @Schema(example = "Faltan precios de uno o mas sectores del evento")
    private String mensaje;
    @Schema(example = "/eventos/1/publicar")
    private String path;
    private List<String> detalles;

    public ApiErrorResponseDTO() {
    }

    public ApiErrorResponseDTO(LocalDateTime timestamp, int status, String error,
                               String mensaje, String path, List<String> detalles) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
        this.detalles = detalles;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<String> getDetalles() { return detalles; }
    public void setDetalles(List<String> detalles) { this.detalles = detalles; }
}
