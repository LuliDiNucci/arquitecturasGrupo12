package com.ticketya.usuarios.controllers;

import com.ticketya.usuarios.dto.*;
import com.ticketya.usuarios.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios")
@SecurityRequirement(name = "bearerJwt")
@ApiResponses({@ApiResponse(responseCode = "401", description = "JWT ausente o inválido"),
    @ApiResponse(responseCode = "403", description = "Permisos insuficientes"),
    @ApiResponse(responseCode = "404", description = "Usuario inexistente")})
public class UsuarioController {
    private final UsuarioService usuarios;
    public UsuarioController(UsuarioService usuarios) { this.usuarios = usuarios; }
    @GetMapping("/me")
    @Operation(summary = "Consultar perfil propio")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) { return usuarios.obtener(Long.valueOf(jwt.getSubject())); }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or #id.toString() == authentication.name")
    @Operation(summary = "Consultar usuario", description = "Sólo el titular o ADMINISTRADOR.")
    public UsuarioResponse obtener(@PathVariable Long id) { return usuarios.obtener(id); }
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Reemplazar roles", description = "Requiere ADMINISTRADOR; se aplican al siguiente login.")
    @ApiResponse(responseCode = "400", description = "Conjunto vacío, nulo o rol desconocido")
    public UsuarioResponse roles(@PathVariable Long id, @Valid @RequestBody RolesRequest request) {
        return usuarios.roles(id, request);
    }
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Habilitar o deshabilitar cuenta", description = "Impide nuevos logins. Los JWT ya emitidos duran hasta exp.")
    @ApiResponse(responseCode = "400", description = "activo debe ser booleano")
    public UsuarioResponse estado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return usuarios.estado(id, request);
    }
}
