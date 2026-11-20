package com.ticketya.observability;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public final class HttpEvents {
    public static final String USER_ATTRIBUTE = HttpEvents.class.getName() + ".user";
    public static final String ERROR_ATTRIBUTE = HttpEvents.class.getName() + ".error";
    private static final Logger LOG = LoggerFactory.getLogger("ticketya.http");
    private HttpEvents() {}

    public static String user(Authentication authentication) {
        // Sólo un JWT que ya atravesó el Resource Server puede identificar al usuario.
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) return jwt.getSubject();
        return "anonimo";
    }
    public static void write(String event, String service, String id, String user, String method,
            String endpoint, int status, long started, String error) {
        var entry = LOG.atLevel(status >= 500 ? Level.ERROR : status >= 400 ? Level.WARN : Level.INFO)
            // servicio lo agrega ticketya-logging.properties a todos los registros.
            .addKeyValue("evento", event)
            .addKeyValue(Correlation.KEY, id).addKeyValue("usuario", user)
            .addKeyValue("metodo", method).addKeyValue("endpoint", endpoint)
            .addKeyValue("status", status)
            .addKeyValue("duracionMs", Math.max(0, (System.nanoTime() - started) / 1_000_000));
        if (error != null || status >= 400) entry.addKeyValue("error", error == null ? "HTTP_" + status : error);
        // Nunca incluir mensajes de excepciones: Feign/SMTP pueden contener cuerpos o credenciales.
        // Logstash rechaza claves duplicadas entre MDC y pares SLF4J. Se toman
        // los valores explícitos para este evento y se restaura el contexto al salir.
        var previous = MDC.getCopyOfContextMap();
        MDC.remove(Correlation.KEY); MDC.remove("usuario");
        try { entry.log(event); }
        finally { if (previous == null) MDC.clear(); else MDC.setContextMap(previous); }
    }
    public static String path(URI uri) {
        String path = uri.getRawPath();
        if (path == null || path.isEmpty()) return "/";
        int parameters = path.indexOf(';');
        if (parameters >= 0) path = path.substring(0, parameters);
        return path.substring(0, Math.min(path.length(), 2048));
    }
}
