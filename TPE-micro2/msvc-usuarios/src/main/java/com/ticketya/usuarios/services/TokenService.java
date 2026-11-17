package com.ticketya.usuarios.services;

import com.ticketya.usuarios.dto.TokenResponse;
import com.ticketya.usuarios.entities.Usuario;

public interface TokenService {
    TokenResponse forUser(Usuario usuario);
    TokenResponse forService(String clientId);
}
