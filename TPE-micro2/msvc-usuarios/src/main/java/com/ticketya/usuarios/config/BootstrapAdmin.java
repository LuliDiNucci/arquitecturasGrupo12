package com.ticketya.usuarios.config;

import com.ticketya.usuarios.entities.*;
import com.ticketya.usuarios.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Configuration
public class BootstrapAdmin {
    @Bean
    ApplicationRunner bootstrap(UsuarioRepository repository, PasswordEncoder encoder,
            PlatformTransactionManager manager,
            @Value("${ticketya.bootstrap.enabled:false}") boolean enabled,
            @Value("${ticketya.bootstrap.email:}") String email,
            @Value("${ticketya.bootstrap.password:}") String password) {
        return args -> {
            if (!enabled) return;
            if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException("Configure email y contraseña válidos para el bootstrap");
            }
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                if (repository.count() != 0) {
                    // Nunca promover una cuenta ya registrada públicamente.
                    if (repository.findByEmail(Usuario.normalizarEmail(email))
                        .filter(u -> u.getRoles().contains(Rol.ADMINISTRADOR)).isPresent()) return;
                    throw new IllegalStateException("Bootstrap requiere una base de usuarios vacía");
                }
                repository.saveAndFlush(new Usuario("Administrador", "Inicial", email,
                    encoder.encode(password), Set.of(Rol.ADMINISTRADOR)));
            });
        };
    }
}
