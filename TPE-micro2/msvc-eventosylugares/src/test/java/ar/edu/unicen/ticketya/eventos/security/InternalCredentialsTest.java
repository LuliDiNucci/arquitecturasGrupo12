package ar.edu.unicen.ticketya.eventos.security;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.DefaultResourceLoader;
import static org.assertj.core.api.Assertions.*;

class InternalCredentialsTest {
    private HttpServer server;
    private final AtomicInteger calls = new AtomicInteger();
    private final AtomicReference<String> basic = new AtomicReference<>();
    private String serviceToken;
    private InternalCredentials credentials;
    @BeforeEach void setup() throws Exception {
        serviceToken = TestTokens.service("eventos");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/auth/service-token", exchange -> {
            calls.incrementAndGet(); basic.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = ("{\"accessToken\":\"" + serviceToken + "\",\"expiresIn\":300,\"tokenType\":\"Bearer\"}")
                .getBytes(StandardCharsets.UTF_8);
            exchange.getRequestBody().close();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body); exchange.close();
        });
        server.start();
        credentials = new InternalCredentials("test-only-secret-with-more-than-32-characters",
            "http://127.0.0.1:" + server.getAddress().getPort() + "/auth/service-token");
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); server.stop(0); }
    private void authenticated(String token) {
        var jwt = JwtSupport.decoder(new DefaultResourceLoader().getResource(TestTokens.PUBLIC), "ticketya-usuarios", "ticketya-api").decode(token);
        SecurityContextHolder.getContext().setAuthentication(JwtSupport.authenticationConverter().convert(jwt));
    }
    @Test void forwardsUserOnlyForReadsAndCachesOwnServiceCredential() {
        String user = TestTokens.user(7, "CLIENTE"); authenticated(user);
        assertThat(credentials.authorization("GET")).isEqualTo("Bearer " + user);
        assertThat(calls.get()).isZero();
        assertThat(credentials.authorization("POST")).isEqualTo("Bearer " + serviceToken);
        assertThat(credentials.authorization("PATCH")).isEqualTo("Bearer " + serviceToken);
        assertThat(calls.get()).isEqualTo(1);
        String expected = "Basic " + java.util.Base64.getEncoder().encodeToString(
            "eventos:test-only-secret-with-more-than-32-characters".getBytes(StandardCharsets.UTF_8));
        assertThat(basic.get()).isEqualTo(expected);
    }
    @Test void scheduledAndNestedServiceCallsUseOwnIdentity() {
        assertThat(credentials.authorization("POST")).isEqualTo("Bearer " + serviceToken);
        authenticated(TestTokens.service("compras"));
        assertThat(credentials.authorization("GET")).isEqualTo("Bearer " + serviceToken);
        assertThat(calls.get()).isEqualTo(1);
    }
    @Test void failsClosedWithoutConfiguredCredentials() {
        var unconfigured = new InternalCredentials("", "http://127.0.0.1:" + server.getAddress().getPort());
        assertThatThrownBy(() -> unconfigured.authorization("POST")).isInstanceOf(IllegalStateException.class);
        assertThat(calls.get()).isZero();
    }
}
