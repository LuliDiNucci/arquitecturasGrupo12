package com.ticketya.observability;

import java.net.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;
import feign.*;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;

class HttpClientCorrelationTest {
    @Test void restClientCarriesIdToRealHttpServerIncludingErrors() throws Exception {
        var received = new AtomicReference<String>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/reservas/1", exchange -> {
            received.set(exchange.getRequestHeaders().getFirst(Correlation.HEADER));
            exchange.sendResponseHeaders(400, -1); exchange.close();
        });
        server.start();
        try (var scope = Correlation.open("purchase-through-services")) {
            var client = RestClient.builder().baseUrl("http://127.0.0.1:" + server.getAddress().getPort())
                .requestInterceptor(new CorrelationHttpInterceptor("entradas")).build();
            assertThatThrownBy(() -> client.get().uri("/reservas/1").retrieve().toBodilessEntity())
                .isInstanceOf(org.springframework.web.client.HttpClientErrorException.class);
            assertThat(received.get()).isEqualTo("purchase-through-services");
        } finally { server.stop(0); }
        assertThat(MDC.get(Correlation.KEY)).isNull();
    }
    @Test void feignReplacesForgedOutgoingHeaderAndKeepsIdOnConnectionFailure() {
        try (var scope = Correlation.open("purchase-failure")) {
            var template = new RequestTemplate().method("GET").target("http://localhost").uri("/reservas/1");
            template.header(Correlation.HEADER, "must-be-replaced");
            new ObservabilityAutoConfiguration.FeignConfiguration().correlationFeignInterceptor().apply(template);
            Client client = new CorrelationFeignCapability("compras").enrich((Client) (request, options) -> {
                assertThat(request.headers().get(Correlation.HEADER)).containsExactly("purchase-failure");
                throw new IOException("NEVER_LOG_THIS");
            });
            assertThatThrownBy(() -> client.execute(template.resolve(Map.of()).request(), new Request.Options()))
                .isInstanceOf(IOException.class);
        }
        assertThat(MDC.get(Correlation.KEY)).isNull();
    }
}
