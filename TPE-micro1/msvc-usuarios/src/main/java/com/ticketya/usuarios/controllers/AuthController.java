package com.ticketya.usuarios.controllers;

import com.ticketya.usuarios.dto.*;
import com.ticketya.usuarios.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación")
public class AuthController {
    private final UsuarioService usuarios;
    private final ServiceAuthenticationService services;
    public AuthController(UsuarioService usuarios, ServiceAuthenticationService services) {
        this.usuarios = usuarios; this.services = services;
    }
    @PostMapping("/register")
    @Operation(summary = "Registrar un cliente", description = "Ignora rol/roles enviados; asigna exclusivamente CLIENTE.")
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cliente registrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Email duplicado")})
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse usuario = usuarios.registrar(request);
        return ResponseEntity.created(URI.create("/usuarios/" + usuario.getId())).body(usuario);
    }
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Entrega un Bearer RS256; cinco fallos por email en quince minutos bloquean temporalmente el login.")
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "200", description = "JWT emitido"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas o cuenta inactiva"),
        @ApiResponse(responseCode = "429", description = "Límite temporal de intentos")})
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(usuarios.login(request));
    }
    @PostMapping("/service-token")
    @Operation(summary = "Obtener credencial interna", description = "HTTP Basic con ID de servicio y secreto configurados. Token de cinco minutos sin roles humanos.")
    @SecurityRequirement(name = "serviceBasic")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Token de servicio emitido"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "429", description = "Límite temporal de intentos")})
    public ResponseEntity<TokenResponse> serviceToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(services.authenticate(authorization));
    }
}
