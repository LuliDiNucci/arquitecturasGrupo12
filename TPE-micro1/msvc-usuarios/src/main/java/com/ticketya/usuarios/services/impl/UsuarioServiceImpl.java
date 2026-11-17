package com.ticketya.usuarios.services.impl;

import com.ticketya.usuarios.dto.*;
import com.ticketya.usuarios.entities.*;
import com.ticketya.usuarios.exceptions.ApiException;
import com.ticketya.usuarios.repositories.UsuarioRepository;
import com.ticketya.usuarios.security.LoginAttemptLimiter;
import com.ticketya.usuarios.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final LoginAttemptLimiter limiter;
    private final String dummyHash;
    public UsuarioServiceImpl(UsuarioRepository repository, PasswordEncoder passwords,
            TokenService tokens, LoginAttemptLimiter limiter) {
        this.repository = repository; this.passwords = passwords; this.tokens = tokens; this.limiter = limiter;
        dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
    }
    @Override @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = Usuario.normalizarEmail(request.getEmail());
        if (repository.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "El email ya está registrado");
        Usuario usuario = new Usuario(request.getNombre().trim(), request.getApellido().trim(), email,
            passwords.encode(request.getPassword()), Set.of(Rol.CLIENTE));
        return new UsuarioResponse(repository.saveAndFlush(usuario));
    }
    @Override @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = Usuario.normalizarEmail(request.getEmail());
        limiter.acquire("user:" + email);
        Usuario usuario = repository.findByEmail(email).orElse(null);
        boolean valid = passwords.matches(request.getPassword(), usuario == null ? dummyHash : usuario.getPasswordHash());
        if (!valid || usuario == null || !usuario.isActivo()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        limiter.success("user:" + email);
        return tokens.forUser(usuario);
    }
    private Usuario find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario inexistente"));
    }
    @Override @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) { return new UsuarioResponse(find(id)); }
    @Override @Transactional
    public UsuarioResponse roles(Long id, RolesRequest request) {
        Usuario usuario = find(id); usuario.setRoles(request.getRoles());
        return new UsuarioResponse(repository.save(usuario));
    }
    @Override @Transactional
    public UsuarioResponse estado(Long id, EstadoRequest request) {
        Usuario usuario = find(id); usuario.setActivo(request.getActivo());
        return new UsuarioResponse(repository.save(usuario));
    }
}
