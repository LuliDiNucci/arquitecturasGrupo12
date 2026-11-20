package com.ticketya.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import com.ticketya.gateway.security.TestTokens;

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
    private static final StubService usuarios = new StubService("usuarios");

    @Autowired
    private WebTestClient webClient;

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void serviceUrls(DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
        registry.add("USUARIOS_URL", usuarios::url);
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
        usuarios.reset();
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
                .header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("reservas");

        assertEquals("POST", reservas.method());
        assertEquals("/reservas?origen=postman", reservas.path());
        assertEquals(body, reservas.body());
        assertTrue(reservas.authorization().startsWith("Bearer "));
        assertEquals("10", signedSubject(reservas.authorization()));
    }

    @Test
    void enrutaComprasYPropagaErroresDelServicio() {
        compras.status = 409;

        webClient.post().uri("/compras")
                .header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(String.class).isEqualTo("compras");
    }

    @Test
    void enrutaEntradasConMetodoYBody() {
        String body = "{\"compraId\":30}";

        webClient.post().uri("/entradas/emitir")
                .header("Authorization", "Bearer " + TestTokens.service("compras"))
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("entradas");

        assertEquals("POST", entradas.method());
        assertEquals("/entradas/emitir", entradas.path());
        assertEquals(body, entradas.body());
    }

    @Test
    void rutaNoConfiguradaSeDeniega() {
        webClient.get().uri("/ruta-inexistente")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void servicioNoDisponibleNoBloqueaLaSolicitud() {
        entradas.stop();

        long start = System.currentTimeMillis();
        webClient.get().uri("/entradas/1")
                .header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
                .exchange()
                .expectStatus().is5xxServerError();
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 10_000,
                "La solicitud no debería quedar bloqueada indefinidamente");
    }

    private String signedSubject(String authorization) {
        try {
            return com.nimbusds.jwt.SignedJWT.parse(authorization.substring(7)).getJWTClaimsSet().getSubject();
        } catch (java.text.ParseException ex) { throw new AssertionError(ex); }
    }

    @Test
    void swaggerAgregadoCargaSuPaginaYConfig() {
        String location = webClient.get().uri("/swagger-ui.html").exchange()
            .expectStatus().is3xxRedirection().returnResult(Void.class).getResponseHeaders().getFirst("Location");
        org.junit.jupiter.api.Assertions.assertNotNull(location);
        webClient.get().uri(location).exchange().expectStatus().isOk();
        webClient.get().uri("/v3/api-docs/swagger-config").exchange().expectStatus().isOk()
            .expectBody().jsonPath("$.urls[*].name").value(org.hamcrest.Matchers.hasItem("Usuarios"));
    }

    @Test
    void usuariosYDocumentacionSeEnrutanYConservanBearer() {
        webClient.post().uri("/auth/register").bodyValue("{}").exchange().expectStatus().isOk();
        assertEquals("/auth/register", usuarios.path());
        webClient.post().uri("/auth/login").bodyValue("{}").exchange().expectStatus().isOk();
        String token = TestTokens.user(10, "CLIENTE");
        webClient.get().uri("/usuarios/me").header("Authorization", "Bearer " + token).exchange().expectStatus().isOk();
        assertEquals("Bearer " + token, usuarios.authorization());
        webClient.get().uri("/usuarios/v3/api-docs").exchange().expectStatus().isOk();
        assertEquals("/v3/api-docs", usuarios.path());
    }

    @Test
    void rechazaTokensInvalidosYExpirados() {
        webClient.get().uri("/usuarios/me").exchange().expectStatus().isUnauthorized();
        webClient.get().uri("/usuarios/me").header("Authorization", "Bearer invalid").exchange().expectStatus().isUnauthorized();
        var claims = TestTokens.claims("10", java.util.List.of("CLIENTE"), "user");
        claims.put("iat", java.time.Instant.now().minusSeconds(100));
        claims.put("exp", java.time.Instant.now().minusSeconds(1));
        webClient.get().uri("/usuarios/me").header("Authorization", "Bearer " + TestTokens.sign(claims, TestTokens.KEYS))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void aplicaRolesAntesDeEnviarAlServicio() {
        for (String role : java.util.List.of("CLIENTE", "ORGANIZADOR", "PERSONAL_ACCESO")) {
            webClient.patch().uri("/devoluciones/1/aprobar")
                .header("Authorization", "Bearer " + TestTokens.user(10, role)).exchange().expectStatus().isForbidden();
            webClient.patch().uri("/usuarios/1/roles")
                .header("Authorization", "Bearer " + TestTokens.user(10, role)).bodyValue("{\"roles\":[\"ADMINISTRADOR\"]}")
                .exchange().expectStatus().isForbidden();
        }
        webClient.patch().uri("/devoluciones/1/aprobar")
            .header("Authorization", "Bearer " + TestTokens.user(10, "ADMINISTRADOR")).exchange().expectStatus().isOk();
        webClient.post().uri("/eventos").header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
            .bodyValue("{}").exchange().expectStatus().isForbidden();
        webClient.post().uri("/reservas/1/confirmar").header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
            .exchange().expectStatus().isForbidden();
        webClient.post().uri("/reservas/1/confirmar").header("Authorization", "Bearer " + TestTokens.service("compras"))
            .exchange().expectStatus().isOk();
    }

    @Test
    void preservesCorrelationInDownstreamAndAllHttpResponses() {
        String id = "compra-123.gateway";
        webClient.get().uri("/eventos/publicados")
            .header("X-Correlation-ID", id).exchange().expectStatus().isOk()
            .expectHeader().valueEquals("X-Correlation-ID", id);
        assertEquals(id, eventos.correlation.get());
        for (int status : java.util.List.of(400, 401, 403, 500)) {
            compras.status = status;
            webClient.post().uri("/compras").header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
                .header("X-Correlation-ID", id).exchange().expectStatus().isEqualTo(status)
                .expectHeader().valueEquals("X-Correlation-ID", id);
            assertEquals(id, compras.correlation.get());
        }
        webClient.get().uri("/usuarios/me").header("X-Correlation-ID", id).exchange()
            .expectStatus().isUnauthorized().expectHeader().valueEquals("X-Correlation-ID", id);
        webClient.post().uri("/entradas/validar").header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
            .header("X-Correlation-ID", id).exchange().expectStatus().isForbidden()
            .expectHeader().valueEquals("X-Correlation-ID", id);
    }

    @Test
    void generatesAndSanitizesCorrelationBeforeRoutingIncludingConnectionFailures() {
        for (String[] incoming : java.util.List.of(new String[]{}, new String[]{"invalid value"},
                new String[]{"a".repeat(65)}, new String[]{"one", "two"})) {
            String id = webClient.get().uri("/eventos/publicados").headers(headers -> {
                for (String value : incoming) headers.add("X-Correlation-ID", value);
            }).exchange().expectStatus().isOk().returnResult(String.class).getResponseHeaders().getFirst("X-Correlation-ID");
            java.util.UUID.fromString(id);
            assertEquals(id, eventos.correlation.get());
        }
        entradas.stop();
        webClient.get().uri("/entradas/1").header("Authorization", "Bearer " + TestTokens.user(10, "CLIENTE"))
            .header("X-Correlation-ID", "connection-failure").exchange().expectStatus().is5xxServerError()
            .expectHeader().valueEquals("X-Correlation-ID", "connection-failure");
    }

    @Test
    void routesQrForStaffButDeniesNotificationWritesForHumanTokens() {
        webClient.post().uri("/entradas/validar").header("Authorization", "Bearer " + TestTokens.user(10, "PERSONAL_ACCESO"))
            .bodyValue("{\"eventoId\":1,\"codigoQr\":\"code\"}").exchange().expectStatus().isOk();
        assertEquals("/entradas/validar", entradas.path());
        for (String role : java.util.List.of("CLIENTE", "ADMINISTRADOR", "ORGANIZADOR", "PERSONAL_ACCESO")) {
            webClient.post().uri("/notificaciones/compras-confirmadas")
                .header("Authorization", "Bearer " + TestTokens.user(10, role)).bodyValue("{}")
                .exchange().expectStatus().isForbidden();
        }
    }

    private static final class StubService {
        private final String response;
        private final AtomicReference<String> method = new AtomicReference<>();
        private final AtomicReference<String> path = new AtomicReference<>();
        private final AtomicReference<String> body = new AtomicReference<>();
        private final AtomicReference<String> authorization = new AtomicReference<>();
        private final AtomicReference<String> correlation = new AtomicReference<>();
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

        private String authorization() { return authorization.get(); }

        private final class RecordingHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                method.set(exchange.getRequestMethod());
                authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                correlation.set(exchange.getRequestHeaders().getFirst("X-Correlation-ID"));
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
