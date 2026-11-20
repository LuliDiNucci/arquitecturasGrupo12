package ar.edu.unicen.ticketya.eventos.security;

import feign.RequestInterceptor;
import org.springframework.context.annotation.*;

@Configuration
public class FeignSecurityConfig {
    @Bean RequestInterceptor bearerForwarding(InternalCredentials credentials) {
        return template -> {
            template.removeHeader("Authorization");
            String url = template.url();
            boolean reporteComprasInterno = url != null
                    && url.contains("/compras/evento/")
                    && url.contains("/reporte");
            String autorizacion = reporteComprasInterno
                    ? credentials.serviceAuthorization()
                    : credentials.authorization(template.method());
            template.header("Authorization", autorizacion);
        };
    }
}

