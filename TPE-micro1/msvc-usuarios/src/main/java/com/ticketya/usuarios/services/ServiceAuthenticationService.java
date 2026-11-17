package com.ticketya.usuarios.services;

import com.ticketya.usuarios.dto.TokenResponse;

public interface ServiceAuthenticationService {
    TokenResponse authenticate(String authorization);
}
