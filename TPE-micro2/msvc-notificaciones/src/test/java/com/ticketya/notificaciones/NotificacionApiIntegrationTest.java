package com.ticketya.notificaciones;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.ticketya.notificaciones.entities.TipoNotificacion;
import com.ticketya.notificaciones.repositories.NotificacionRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificacionApiIntegrationTest {

    private static final GreenMail SMTP = iniciarSmtp();
    private static final JsonMapper JSON = new JsonMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    private int puerto;
    @Autowired
    private NotificacionRepository repository;

    @DynamicPropertySource
    static void configurarSmtp(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> SMTP.getSmtp().getPort());
    }

    @AfterAll
    static void cerrarSmtp() {
        SMTP.stop();
    }

    @Test
    void aceptaUnaCompraYElProcesoProgramadoEntregaElCorreo() throws Exception {
        String cuerpo = compra(101, 12, "cliente@compra.test");
        var respuesta = post("/compras-confirmadas", cuerpo);
        assertThat(respuesta.statusCode()).isEqualTo(202);
        JsonNode registrada = JSON.readTree(respuesta.body());
        assertThat(registrada.path("estado").asString()).isEqualTo("PENDIENTE");
        assertThat(registrada.path("intentos").asInt()).isZero();
        assertThat(registrada.has("email")).isFalse();

        String ruta = respuesta.headers().firstValue("Location").orElseThrow();
        assertThat(ruta).isEqualTo("/notificaciones/" + registrada.path("id").asLong());
        esperarEnvio(ruta);

        var buzon = SMTP.getReceivedMessagesForDomain("compra.test");
        assertThat(buzon).hasSize(1);
        assertThat(buzon[0].getAllRecipients()[0].toString()).isEqualTo("cliente@compra.test");
        assertThat(buzon[0].getSubject()).isEqualTo("TicketYa | Compra confirmada");
        assertThat(buzon[0].getContent().toString()).contains("#101", "Recital en Tandil");

        var duplicada = post("/compras-confirmadas", cuerpo);
        assertThat(duplicada.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(duplicada.body()).path("id")).isEqualTo(registrada.path("id"));
        assertThat(JSON.readTree(duplicada.body()).path("intentos").asInt()).isEqualTo(1);
    }

    @Test
    void conservaUnaSolaFilaAnteSolicitudesSimultaneas() throws Exception {
        var solicitudes = IntStream.range(0, 8)
                .mapToObj(i -> http.sendAsync(solicitudPost("/compras-confirmadas",
                        compra(102, 12, "cliente@concurrente.test")), HttpResponse.BodyHandlers.ofString()))
                .toList();

        long id = -1;
        for (var solicitud : solicitudes) {
            var respuesta = solicitud.get(10, TimeUnit.SECONDS);
            assertThat(respuesta.statusCode()).isIn(200, 202);
            long idDevuelto = JSON.readTree(respuesta.body()).path("id").asLong();
            assertThat(idDevuelto).isPositive();
            if (id == -1) {
                id = idDevuelto;
            }
            assertThat(idDevuelto).isEqualTo(id);
        }

        esperarEnvio("/notificaciones/" + id);
        var coincidentes = repository.findAll().stream()
                .filter(aviso -> aviso.getTipo() == TipoNotificacion.COMPRA_CONFIRMADA && aviso.getReferenciaId() == 102)
                .toList();
        assertThat(coincidentes).hasSize(1);
        assertThat(SMTP.getReceivedMessagesForDomain("concurrente.test")).hasSize(1);
    }

    @Test
    void distingueCancelacionModificacionesYDestinatarios() throws Exception {
        String evento = """
                {"destinatario":{"clienteId":12,"email":"eventos@example.test"},
                "eventoId":45,"nombreEvento":"Recital en Tandil"}
                """;
        long cancelacion = idAceptado(post("/eventos-cancelados", evento));
        long clienteDistinto = idAceptado(post("/eventos-cancelados", evento.replace("12", "14")));
        long primerCambio = idAceptado(post("/eventos-modificados", modificacion(1)));
        long segundoCambio = idAceptado(post("/eventos-modificados", modificacion(2)));

        assertThat(List.of(cancelacion, clienteDistinto, primerCambio, segundoCambio)).doesNotHaveDuplicates();
        var duplicada = post("/eventos-modificados", modificacion(1));
        assertThat(duplicada.statusCode()).isIn(200, 202);
        assertThat(JSON.readTree(duplicada.body()).path("id").asLong()).isEqualTo(primerCambio);
    }

    @Test
    void aceptaSoloArtistasFavoritosQueSePresentanEnLaCiudadDelCliente() throws Exception {
        String cuerpo = """
                {"destinatario":{"clienteId":12,"email":"favoritos@example.test"},
                "eventoId":50,"nombreEvento":"Recital en Tandil","fechaHora":"2026-12-05T21:00:00",
                "ciudadEvento":"Tandil","artistaId":8,"nombreArtista":"Los Viajeros",
                "ciudadCliente":" tandil ","artistasFavoritosIds":[8,15]}
                """;

        var otraCiudad = post("/artistas-favoritos", cuerpo.replace(" tandil ", "Azul"));
        assertThat(otraCiudad.statusCode()).isEqualTo(204);
        assertThat(otraCiudad.body()).isEmpty();
        var otroArtista = post("/artistas-favoritos", cuerpo.replace("[8,15]", "[15]"));
        assertThat(otroArtista.statusCode()).isEqualTo(204);

        long favorita = idAceptado(post("/artistas-favoritos", cuerpo));
        var mismoEvento = post("/artistas-favoritos", cuerpo.replace("\"artistaId\":8", "\"artistaId\":15"));
        assertThat(mismoEvento.statusCode()).isIn(200, 202);
        assertThat(JSON.readTree(mismoEvento.body()).path("id").asLong()).isEqualTo(favorita);
    }

    @Test
    void informaErroresDeValidacionYDeBusqueda() throws Exception {
        var invalida = post("/compras-confirmadas", compra(-4, 0, "sin-arroba"));
        assertThat(invalida.statusCode()).isEqualTo(400);
        assertThat(invalida.headers().firstValue("Content-Type").orElseThrow())
                .startsWith("application/problem+json");
        assertThat(get("/notificaciones/0").statusCode()).isEqualTo(400);
        assertThat(get("/notificaciones/1234567").statusCode()).isEqualTo(404);
    }

    @Test
    void publicaLosContratosEnOpenApi() throws Exception {
        var respuesta = get("/v3/api-docs");
        assertThat(respuesta.statusCode()).isEqualTo(200);
        var rutas = JSON.readTree(respuesta.body()).path("paths");
        assertThat(rutas.has("/notificaciones/compras-confirmadas")).isTrue();
        assertThat(rutas.has("/notificaciones/eventos-cancelados")).isTrue();
        assertThat(rutas.has("/notificaciones/eventos-modificados")).isTrue();
        assertThat(rutas.path("/notificaciones/artistas-favoritos").path("post")
                .path("responses").has("204")).isTrue();
        assertThat(rutas.path("/notificaciones/{id}").path("get").path("responses").has("404")).isTrue();
    }

    private void esperarEnvio(String ruta) {
        await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
            var consulta = get(ruta);
            assertThat(consulta.statusCode()).isEqualTo(200);
            assertThat(JSON.readTree(consulta.body()).path("estado").asString()).isEqualTo("ENVIADA");
        });
    }

    private long idAceptado(HttpResponse<String> respuesta) {
        assertThat(respuesta.statusCode()).isEqualTo(202);
        return JSON.readTree(respuesta.body()).path("id").asLong();
    }

    private static String compra(long compraId, long clienteId, String email) {
        return """
                {"destinatario":{"clienteId":%d,"email":"%s"},
                "compraId":%d,"nombreEvento":"Recital en Tandil"}
                """.formatted(clienteId, email, compraId);
    }

    private static String modificacion(int version) {
        return """
                {"destinatario":{"clienteId":12,"email":"eventos@example.test"},
                "eventoId":45,"versionEvento":%d,"nombreEvento":"Recital en Tandil",
                "detalleCambios":"La apertura de puertas se pasa a las 20:30."}
                """.formatted(version);
    }

    private HttpResponse<String> post(String ruta, String cuerpo) throws Exception {
        return http.send(solicitudPost(ruta, cuerpo), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest solicitudPost(String ruta, String cuerpo) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/notificaciones" + ruta))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
    }

    private HttpResponse<String> get(String ruta) throws Exception {
        var solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .timeout(Duration.ofSeconds(10))
                .GET().build();
        return http.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }

    private static GreenMail iniciarSmtp() {
        var servidor = new GreenMail(new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
        servidor.start();
        return servidor;
    }
}
