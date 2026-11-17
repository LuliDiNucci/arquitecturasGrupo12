package com.ticketya.compras.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentIdentity {
    private CurrentIdentity() {}
    public static Authentication authentication() { return SecurityContextHolder.getContext().getAuthentication(); }
    public static boolean authority(String name) {
        Authentication auth = authentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(name));
    }
    public static Long userId() {
        Authentication auth = authentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt && "user".equals(jwt.getClaimAsString("token_use")))
            return Long.valueOf(jwt.getSubject());
        return null;
    }
    public static boolean owner(Long id) { return id != null && id.equals(userId()); }
    public static boolean admin() { return authority("ROLE_ADMINISTRADOR"); }
}
