package com.ticketya.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MsvcGatewayApplicationTests {

    private static final StubService eventos = new StubService("eventos");
    private static final StubService reservas = new StubService("reservas");
    private static final StubService compras = new StubService("compras");
    private static final StubService entradas = new StubService("entradas");

    @Autowired
    private WebTestClient webClient;

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void serviceUrls(DynamicPropertyRegistry registry) {
        registry.add("EVENTOS_URL", eventos::url);
        registry.add("RESERVAS_URL", reservas::url);
        registry.add("COMPRAS_URL", compras::url);
        registry.add("ENTRADAS_URL", entradas::url);
    }

    @AfterEach
    void restartStubs() {
        eventos.start();
        reservas.start();
        compras.start();
        entradas.start();
        eventos.reset();
        reservas.reset();
        compras.reset();
        entradas.reset();
    }

    @Test
    void healthCheckRespondeCorrectamente() {
        webClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void enrutaEventosAlPuerto8081Configurado() {
        webClient.get().uri("/eventos/publicados")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("eventos");

        assertEquals("GET", eventos.method());
        assertEquals("/eventos/publicados", eventos.path());
    }

    @Test
    void enrutaReservasConParametrosYBody() {
        String body = "{\"clienteId\":10,\"eventoId\":20}";

        webClient.post().uri("/reservas?origen=postman")
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("reservas");

        assertEquals("POST", reservas.method());
        assertEquals("/reservas?origen=postman", reservas.path());
        assertEquals(body, reservas.body());
    }

    @Test
    void enrutaComprasYPropagaErroresDelServicio() {
        compras.status = 409;

        webClient.post().uri("/compras/error")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(String.class).isEqualTo("compras");
    }

    @Test
    void enrutaEntradasConMetodoYBody() {
        String body = "{\"compraId\":30}";

        webClient.patch().uri("/entradas/1")
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("entradas");

        assertEquals("PATCH", entradas.method());
        assertEquals("/entradas/1", entradas.path());
        assertEquals(body, entradas.body());
    }

    @Test
    void rutaNoConfiguradaResponde404() {
        webClient.get().uri("/ruta-inexistente")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void servicioNoDisponibleNoBloqueaLaSolicitud() {
        entradas.stop();

        long start = System.currentTimeMillis();
        webClient.get().uri("/entradas/1")
                .exchange()
                .expectStatus().is5xxServerError();
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 10_000,
                "La solicitud no debería quedar bloqueada indefinidamente");
    }

    private static final class StubService {
        private final String response;
        private final AtomicReference<String> method = new AtomicReference<>();
        private final AtomicReference<String> path = new AtomicReference<>();
        private final AtomicReference<String> body = new AtomicReference<>();
        private final int port;
        private volatile HttpServer server;
        private volatile int status = 200;

        private StubService(String response) {
            this.response = response;
            try {
                HttpServer initialServer =
                        HttpServer.create(new InetSocketAddress(0), 0);
                port = initialServer.getAddress().getPort();
                server = initialServer;
                server.createContext("/", new RecordingHandler());
                server.start();
            } catch (IOException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }

        private String url() {
            return "http://localhost:" + port;
        }

        private String method() {
            return method.get();
        }

        private String path() {
            return path.get();
        }

        private String body() {
            return body.get();
        }

        private void stop() {
            HttpServer current = server;
            if (current != null) {
                current.stop(0);
                server = null;
            }
        }

        private void start() {
            if (server == null) {
                try {
                    HttpServer restartedServer =
                            HttpServer.create(new InetSocketAddress(port), 0);
                    restartedServer.createContext("/", new RecordingHandler());
                    restartedServer.start();
                    server = restartedServer;
                } catch (IOException exception) {
                    throw new IllegalStateException(
                            "No se pudo reiniciar el stub HTTP", exception);
                }
            }
        }

        private void reset() {
            status = 200;
        }

        private final class RecordingHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                method.set(exchange.getRequestMethod());
                path.set(exchange.getRequestURI().toString());
                try (InputStream input = exchange.getRequestBody()) {
                    body.set(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status, responseBytes.length);
                try {
                    exchange.getResponseBody().write(responseBytes);
                } finally {
                    exchange.close();
                }
            }
        }
    }
}
