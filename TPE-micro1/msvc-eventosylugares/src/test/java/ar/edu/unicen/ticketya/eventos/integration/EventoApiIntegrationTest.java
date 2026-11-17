package ar.edu.unicen.ticketya.eventos.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventoApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    private JsonNode enviar(String ruta, String json) throws Exception {
        String respuesta = mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta);
    }

    private Registro crearBase(boolean sectorNumerado) throws Exception {
        long organizadorId = enviar("/organizadores", "{\"nombre\":\"Org\",\"email\":\"org@ejemplo.com\"}").get("id").asLong();
        long participanteId = enviar("/participantes", "{\"nombre\":\"Banda\",\"tipo\":\"ARTISTA\"}").get("id").asLong();
        long lugarId = enviar("/lugares", "{\"nombre\":\"Teatro\",\"direccion\":\"Calle 1\",\"ciudad\":\"Tandil\",\"latitud\":-37.32,\"longitud\":-59.14}").get("id").asLong();
        long sectorId = enviar("/lugares/" + lugarId + "/sectores",
                "{\"nombre\":\"Platea\",\"numerado\":" + sectorNumerado + ",\"capacidad\":2}").get("id").asLong();
        String fecha = LocalDateTime.now().plusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String evento = "{\"nombre\":\"Recital\",\"descripcion\":\"Banda en vivo\",\"tipo\":\"RECITAL\","
                + "\"organizadorId\":" + organizadorId + ",\"lugarId\":" + lugarId + ","
                + "\"fechaHora\":\"" + fecha + "\",\"participantesIds\":[" + participanteId + "],\"altaDemanda\":false}";
        long eventoId = enviar("/eventos", evento).get("id").asLong();
        return new Registro(eventoId, sectorId, sectorNumerado);
    }

    private void cargarPrecios(Registro registro) throws Exception {
        mvc.perform(put("/eventos/{id}/precios", registro.eventoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precios\":[{\"sectorId\":" + registro.sectorId + ",\"precio\":35000.00}]}"))
                .andExpect(status().isOk());
    }

    private record Registro(long eventoId, long sectorId, boolean numerado) {}

    @Test
    void listadoPublicoSeparaEstadosSinRomperAdministracion() throws Exception {
        Registro registro = crearBase(false);
        cargarPrecios(registro);
        mvc.perform(get("/eventos/publicados")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/eventos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("BORRADOR")));
        mvc.perform(get("/eventos/buscar").param("incluirNoPublicados", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(patch("/eventos/{id}/publicar", registro.eventoId)).andExpect(status().isOk());
        mvc.perform(patch("/eventos/{id}/agotar", registro.eventoId)).andExpect(status().isOk());
        mvc.perform(get("/eventos/publicados")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("AGOTADO")));
        mvc.perform(patch("/eventos/{id}/suspender", registro.eventoId)).andExpect(status().isOk());
        mvc.perform(get("/eventos/publicados")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(patch("/eventos/{id}/cancelar", registro.eventoId)).andExpect(status().isOk());
        mvc.perform(get("/eventos/publicados")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/eventos/{id}", registro.eventoId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
        mvc.perform(get("/eventos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("CANCELADO")));
    }

    @Test
    void buscarSoloMuestraPublicadosYPermiteFiltrarPorCiudad() throws Exception {
        Registro registro = crearBase(false);
        cargarPrecios(registro);

        // Un BORRADOR no aparece en la busqueda publica.
        mvc.perform(get("/eventos/buscar").param("ciudad", "Tandil"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(patch("/eventos/{id}/publicar", registro.eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("PUBLICADO")));

        mvc.perform(get("/eventos/buscar").param("ciudad", "tandil")
                        .param("tipo", "RECITAL").param("ordenar", "PRECIO_ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) registro.eventoId)))
                .andExpect(jsonPath("$[0].precioDesde", is(35000.0)));

        mvc.perform(patch("/eventos/{id}/cancelar", registro.eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));

        mvc.perform(get("/eventos/buscar").param("ciudad", "Tandil"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void sectorNumeradoDebeTenerTodosLosAsientosAntesDePublicar() throws Exception {
        Registro registro = crearBase(true);
        cargarPrecios(registro);

        mvc.perform(patch("/eventos/{id}/publicar", registro.eventoId))
                .andExpect(status().isConflict());

        enviar("/sectores/" + registro.sectorId + "/asientos",
                "{\"asientos\":[{\"fila\":\"A\",\"numero\":1},{\"fila\":\"A\",\"numero\":2}]}");

        mvc.perform(patch("/eventos/{id}/publicar", registro.eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("PUBLICADO")));

        mvc.perform(get("/eventos/{id}/habilitado", registro.eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habilitadoParaReservar", is(true)));

        mvc.perform(get("/eventos/{id}/sectores", registro.eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].asientos", hasSize(2)));
    }

    @Test
    void cancelarCreaUnAvisoParaLosTresMicroservicios() throws Exception {
        Registro registro = crearBase(false);
        cargarPrecios(registro);
        mvc.perform(patch("/eventos/{id}/publicar", registro.eventoId)).andExpect(status().isOk());
        mvc.perform(patch("/eventos/{id}/cancelar", registro.eventoId)).andExpect(status().isOk());

        mvc.perform(get("/eventos/{id}/notificaciones-cancelacion", registro.eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservasNotificadas", is(false)))
                .andExpect(jsonPath("$.entradasNotificadas", is(false)))
                .andExpect(jsonPath("$.comprasNotificadas", is(false)));

        // Repetir la cancelacion no debe generar un segundo aviso.
        mvc.perform(patch("/eventos/{id}/cancelar", registro.eventoId))
                .andExpect(status().isOk());
    }

    @Test
    void noPermiteRepetirAsientoEnElMismoSector() throws Exception {
        Registro registro = crearBase(true);
        mvc.perform(post("/sectores/{id}/asientos", registro.sectorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"asientos\":[{\"fila\":\"A\",\"numero\":1},{\"fila\":\"a\",\"numero\":1}]}"))
                .andExpect(status().isConflict());
    }
    @Test
    void buscaPorNombreDeParticipanteYFiltraPorFechas() throws Exception {
        Registro registro = crearBase(false);
        cargarPrecios(registro);

        mvc.perform(patch("/eventos/{id}/publicar", registro.eventoId))
                .andExpect(status().isOk());

        mvc.perform(get("/eventos/buscar")
                        .param("q", "Banda")
                        .param("ordenar", "RELEVANCIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/eventos/buscar")
                        .param("desde", "2035-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

}
