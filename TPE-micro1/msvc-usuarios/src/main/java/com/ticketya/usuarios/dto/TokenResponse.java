package com.ticketya.usuarios.dto;

import java.time.Instant;

public class TokenResponse {
    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresIn;
    private final Instant expiresAt;
    private final UsuarioResponse usuario;
    public TokenResponse(String accessToken, Instant expiresAt, long expiresIn, UsuarioResponse usuario) {
        this.accessToken = accessToken; this.expiresAt = expiresAt;
        this.expiresIn = expiresIn; this.usuario = usuario;
    }
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public Instant getExpiresAt() { return expiresAt; }
    public UsuarioResponse getUsuario() { return usuario; }
}
