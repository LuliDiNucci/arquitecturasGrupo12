package ar.edu.unicen.ticketya.eventos.integration;

import ar.edu.unicen.ticketya.eventos.model.Organizador;
import ar.edu.unicen.ticketya.eventos.repository.OrganizadorRepository;
import ar.edu.unicen.ticketya.eventos.security.TestTokens;
import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrganizadorUsuarioIntegrationTest {
    private static final AtomicInteger consultas = new AtomicInteger();
    private static final AtomicInteger estadoRemoto = new AtomicInteger(200);
    private static final AtomicReference<String> cuerpoRemoto = new AtomicReference<>();
    private static final AtomicReference<String> autorizacion = new AtomicReference<>();
    private static final AtomicReference<String> ruta = new AtomicReference<>();
    private static final AtomicReference<String> metodo = new AtomicReference<>();
    private static final HttpServer usuarios = iniciarUsuarios();

    @Autowired private MockMvc mvc;
    @Autowired private OrganizadorRepository organizadores;
    @Autowired private EntityManager entityManager;

    private Long organizadorId;
    private String bearerAdministrador;

    private static HttpServer iniciarUsuarios() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/usuarios/", exchange -> {
                consultas.incrementAndGet();
                autorizacion.set(exchange.getRequestHeaders().getFirst("Authorization"));
                ruta.set(exchange.getRequestURI().getPath());
                metodo.set(exchange.getRequestMethod());
                exchange.getRequestBody().close();
                if (estadoRemoto.get() == 0) {
                    exchange.close(); // Simula interrupción de conexión sin una respuesta HTTP.
                    return;
                }
                byte[] body = cuerpoRemoto.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(estadoRemoto.get(), body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
        registry.add("app.integraciones.usuarios-base-url",
                () -> "http://127.0.0.1:" + usuarios.getAddress().getPort());
    }

    @AfterAll
    static void detenerUsuarios() {
        usuarios.stop(0);
    }

    @BeforeEach
    void preparar() {
        consultas.set(0);
        estadoRemoto.set(200);
        cuerpoRemoto.set("{\"id\":22,\"nombre\":\"Ana\",\"apellido\":\"Perez\","
                + "\"email\":\"ana@example.com\",\"activo\":true,"
                + "\"roles\":[\"CLIENTE\",\"ORGANIZADOR\"],\"fechaCreacion\":\"2026-11-16T20:00:00Z\"}");
        autorizacion.set(null);
        ruta.set(null);
        metodo.set(null);
        Organizador organizador = new Organizador("Producciones", "producciones@example.com");
        organizador.setUsuarioId(11L);
        organizadorId = organizadores.saveAndFlush(organizador).getId();
        bearerAdministrador = "Bearer " + TestTokens.user(900, "ADMINISTRADOR");
    }

    private void vincular(int estadoEsperado) throws Exception {
        mvc.perform(patch("/organizadores/{id}/usuario", organizadorId)
                .header("Authorization", bearerAdministrador)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":22}"))
            .andExpect(status().is(estadoEsperado));
    }

    private void comprobarVinculo(Long usuarioId) {
        entityManager.clear();
        assertThat(organizadores.findById(organizadorId).orElseThrow().getUsuarioId()).isEqualTo(usuarioId);
    }

    @Test
    void vinculaUsuarioActivoOrganizadorYPropagaJwtDelAdministrador() throws Exception {
        vincular(200);
        comprobarVinculo(22L);
        assertThat(consultas.get()).isEqualTo(1);
        assertThat(metodo.get()).isEqualTo("GET");
        assertThat(ruta.get()).isEqualTo("/usuarios/22");
        assertThat(autorizacion.get()).isEqualTo(bearerAdministrador);
    }

    @Test
    void rechazaUsuarioInactivoSinCambiarVinculo() throws Exception {
        cuerpoRemoto.set("{\"id\":22,\"activo\":false,\"roles\":[\"ORGANIZADOR\"]}");
        vincular(409);
        comprobarVinculo(11L);
    }

    @Test
    void rechazaUsuarioSinRolOrganizadorSinCambiarVinculo() throws Exception {
        cuerpoRemoto.set("{\"id\":22,\"activo\":true,\"roles\":[\"CLIENTE\"]}");
        vincular(409);
        comprobarVinculo(11L);
    }

    @ParameterizedTest
    @CsvSource({"404,404", "401,503", "403,503", "500,503", "503,503"})
    void manejaErroresDeUsuariosSinGuardar(int remoto, int esperado) throws Exception {
        estadoRemoto.set(remoto);
        cuerpoRemoto.set("{\"error\":\"No se pudo consultar el usuario\"}");
        vincular(esperado);
        comprobarVinculo(11L);
    }

    @Test
    void rechazaConexionInterrumpidaSinCambiarVinculo() throws Exception {
        estadoRemoto.set(0);
        vincular(503);
        comprobarVinculo(11L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "null", "{}", "{\"id\":99,\"activo\":true,\"roles\":[\"ORGANIZADOR\"]}",
        "{\"id\":22,\"roles\":[\"ORGANIZADOR\"]}", "{\"id\":22,\"activo\":true}"
    })
    void rechazaRespuestaIncompletaOInconsistenteSinCambiarVinculo(String body) throws Exception {
        cuerpoRemoto.set(body);
        vincular(503);
        comprobarVinculo(11L);
    }

    @Test
    void noConsultaUsuariosSiElOrganizadorNoExiste() throws Exception {
        mvc.perform(patch("/organizadores/0/usuario")
                .header("Authorization", bearerAdministrador)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":22}"))
            .andExpect(status().isNotFound());
        assertThat(consultas.get()).isZero();
    }

    @Test
    void rechazaClienteSinConsultarUsuarios() throws Exception {
        mvc.perform(patch("/organizadores/{id}/usuario", organizadorId)
                .header("Authorization", "Bearer " + TestTokens.user(900, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":22}"))
            .andExpect(status().isForbidden());
        assertThat(consultas.get()).isZero();
        comprobarVinculo(11L);
    }
}
