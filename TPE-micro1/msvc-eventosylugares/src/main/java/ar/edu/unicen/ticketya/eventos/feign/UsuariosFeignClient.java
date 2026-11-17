package ar.edu.unicen.ticketya.eventos.feign;

import ar.edu.unicen.ticketya.eventos.dto.UsuarioValidacionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** GET propaga el JWT ADMINISTRADOR con el interceptor Feign existente. */
@FeignClient(name = "msvc-usuarios", url = "${app.integraciones.usuarios-base-url}")
public interface UsuariosFeignClient {
    @GetMapping("/usuarios/{id}")
    UsuarioValidacionResponseDTO obtenerUsuario(@PathVariable("id") Long usuarioId);
}
