package com.ticketya.compras.clients;

import com.ticketya.compras.dtos.UsuarioContactoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuariosClient", url = "${msvc.usuarios.url}")
public interface UsuarioClient {
    @GetMapping("/usuarios/{id}/contacto")
    UsuarioContactoDTO obtenerContacto(@PathVariable("id") Long id);
}
