package com.ticketya.usuarios.services;

import com.ticketya.usuarios.dto.*;

public interface UsuarioService {
    UsuarioResponse registrar(RegistroRequest request);
    TokenResponse login(LoginRequest request);
    UsuarioResponse obtener(Long id);
    UsuarioResponse roles(Long id, RolesRequest request);
    UsuarioResponse estado(Long id, EstadoRequest request);
}
