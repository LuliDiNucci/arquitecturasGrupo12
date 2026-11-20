package com.ticketya.observability;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;

/** El identificador transporta trazabilidad, nunca identidad ni autorización. */
public final class Correlation {
    public static final String HEADER = "X-Correlation-ID";
    public static final String KEY = "correlationId";
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");
    private Correlation() {}

    public static String validateOrNew(String value) {
        return value != null && VALID.matcher(value).matches() ? value : UUID.randomUUID().toString();
    }
    public static String currentOrNew() { return validateOrNew(MDC.get(KEY)); }

    /** Restaura el contexto anterior también cuando falla una tarea programada. */
    public static Scope open(String value) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        MDC.put(KEY, validateOrNew(value));
        MDC.remove("usuario");
        return () -> { if (previous == null) MDC.clear(); else MDC.setContextMap(previous); };
    }
    public interface Scope extends AutoCloseable { @Override void close(); }
}
