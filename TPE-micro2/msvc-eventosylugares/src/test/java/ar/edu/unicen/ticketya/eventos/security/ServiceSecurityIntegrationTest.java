package ar.edu.unicen.ticketya.eventos.security;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.unicen.ticketya.eventos.controller.AsientoController;
import ar.edu.unicen.ticketya.eventos.controller.EventoController;
import ar.edu.unicen.ticketya.eventos.controller.LugarController;
import ar.edu.unicen.ticketya.eventos.controller.OrganizadorController;
import ar.edu.unicen.ticketya.eventos.controller.ParticipanteController;
import ar.edu.unicen.ticketya.eventos.controller.SectorController;
import ar.edu.unicen.ticketya.eventos.feign.ReservasFeignClient;
import ar.edu.unicen.ticketya.eventos.model.EstadoEvento;
import ar.edu.unicen.ticketya.eventos.model.Evento;
import ar.edu.unicen.ticketya.eventos.model.Organizador;
import ar.edu.unicen.ticketya.eventos.repository.EventoRepository;
import ar.edu.unicen.ticketya.eventos.repository.OrganizadorRepository;
import ar.edu.unicen.ticketya.eventos.service.AsientoService;
import ar.edu.unicen.ticketya.eventos.service.AvisoArtistasFavoritosService;
import ar.edu.unicen.ticketya.eventos.service.CatalogoCacheService;
import ar.edu.unicen.ticketya.eventos.service.EventoService;
import ar.edu.unicen.ticketya.eventos.service.LugarService;
import ar.edu.unicen.ticketya.eventos.service.OrganizadorService;
import ar.edu.unicen.ticketya.eventos.service.ParticipanteService;
import ar.edu.unicen.ticketya.eventos.service.SectorService;


@WebMvcTest({EventoController.class, OrganizadorController.class, LugarController.class,
    SectorController.class, AsientoController.class, ParticipanteController.class})
@Import({SecurityConfig.class, EventAccess.class})
class ServiceSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean EventoService service;
    @MockitoBean AvisoArtistasFavoritosService avisoArtistasFavoritosService;
    @MockitoBean CatalogoCacheService catalogoCache;
    @MockitoBean OrganizadorService organizers;
    @MockitoBean LugarService places;
    @MockitoBean SectorService sectors;
    @MockitoBean AsientoService seats;
    @MockitoBean ParticipanteService participants;
    @MockitoBean ReservasFeignClient reservas;
    @MockitoBean EventoRepository repository;
    @MockitoBean OrganizadorRepository organizerRepository;
    @DynamicPropertySource static void key(DynamicPropertyRegistry r) {
        r.add("ticketya.jwt.public-key", () -> TestTokens.PUBLIC);
    }
    @BeforeEach void owners() {
        var own = new Organizador("Organizador", "org@example.com"); own.setUsuarioId(7L);
        var other = new Organizador("Otro", "other@example.com"); other.setUsuarioId(8L);
        when(organizerRepository.findById(10L)).thenReturn(Optional.of(own));
        when(organizerRepository.findById(20L)).thenReturn(Optional.of(other));
        Evento event = mock(Evento.class); when(event.getOrganizador()).thenReturn(own); when(event.getEstado()).thenReturn(EstadoEvento.BORRADOR);
        Evento foreign = mock(Evento.class); when(foreign.getOrganizador()).thenReturn(other); when(foreign.getEstado()).thenReturn(EstadoEvento.BORRADOR);
        Evento published = mock(Evento.class); when(published.getEstado()).thenReturn(EstadoEvento.PUBLICADO);
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(repository.findById(2L)).thenReturn(Optional.of(foreign));
        when(repository.findById(3L)).thenReturn(Optional.of(published));
    }
    private String bearer(String role) {
        return "Bearer " + (role.startsWith("service:") ? TestTokens.service(role.substring(8)) : TestTokens.user(7, role));
    }
    static Stream<Arguments> forbiddenEndpoints() {
        List<String[]> endpoints = List.of(
            new String[]{"POST", "/eventos", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"GET", "/eventos", "ADMINISTRADOR"},
            new String[]{"PUT", "/eventos/1", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"PUT", "/eventos/1/precios", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"PATCH", "/eventos/1/publicar", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"PATCH", "/eventos/1/agotar", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"PATCH", "/eventos/1/reabrir", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"PATCH", "/eventos/1/finalizar", "ORGANIZADOR,ADMINISTRADOR"},
            new String[]{"PATCH", "/eventos/1/suspender", "ADMINISTRADOR"},
            new String[]{"PATCH", "/eventos/1/cancelar", "ADMINISTRADOR"},
            new String[]{"GET", "/eventos/1/notificaciones-cancelacion", "ADMINISTRADOR"},
            new String[]{"POST", "/lugares", "ADMINISTRADOR"},
            new String[]{"POST", "/lugares/1/sectores", "ADMINISTRADOR"},
            new String[]{"POST", "/sectores/1/asientos", "ADMINISTRADOR"},
            new String[]{"POST", "/organizadores", "ADMINISTRADOR"},
            new String[]{"GET", "/organizadores", "ADMINISTRADOR"},
            new String[]{"PATCH", "/organizadores/1/usuario", "ADMINISTRADOR"},
            new String[]{"POST", "/participantes", "ADMINISTRADOR"});
        return endpoints.stream().flatMap(e -> Stream.of("CLIENTE", "ORGANIZADOR", "ADMINISTRADOR", "PERSONAL_ACCESO",
            "service:eventos", "service:reservas", "service:compras", "service:entradas")
            .filter(role -> !Arrays.asList(e[2].split(",")).contains(role))
            .map(role -> Arguments.of(e[0], e[1], role)));
    }
    @ParameterizedTest @MethodSource("forbiddenEndpoints")
    void enforcesRolesForEveryAdministrativeEndpoint(String method, String path, String role) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path).header("Authorization", bearer(role))
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }
    @Test void organizerCannotModifyOrPublishForeignEvent() throws Exception {
        mvc.perform(patch("/eventos/2/publicar").header("Authorization", bearer("ORGANIZADOR"))).andExpect(status().isForbidden());
        mvc.perform(put("/eventos/2/precios").header("Authorization", bearer("ORGANIZADOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"precios\":[{\"sectorId\":1,\"precio\":100}]}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void organizerCannotCreateWithSomeoneElsesOrganizerOrTransferOwnEvent() throws Exception {
        String body = "{\"nombre\":\"Recital\",\"descripcion\":\"Banda en vivo\",\"tipo\":\"RECITAL\","
            + "\"organizadorId\":20,\"lugarId\":1,\"fechaHora\":\"2027-12-20T21:00:00\",\"participantesIds\":[1]}";
        mvc.perform(post("/eventos").header("Authorization", bearer("ORGANIZADOR")).contentType(MediaType.APPLICATION_JSON)
            .content(body)).andExpect(status().isForbidden());
        mvc.perform(put("/eventos/1").header("Authorization", bearer("ORGANIZADOR")).contentType(MediaType.APPLICATION_JSON)
            .content(body)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void organizerCanConfigureOwnEventAndAdminCanCancelAnyEvent() throws Exception {
        mvc.perform(put("/eventos/1/precios").header("Authorization", bearer("ORGANIZADOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"precios\":[{\"sectorId\":1,\"precio\":100}]}"))
            .andExpect(status().isOk());
        mvc.perform(patch("/eventos/2/cancelar").header("Authorization", bearer("ADMINISTRADOR"))).andExpect(status().isOk());
        verify(service).configurarPrecios(eq(1L), any()); verify(service).cancelar(2L);
    }
    @Test void publicSearchCannotRevealDrafts() throws Exception {
        mvc.perform(get("/eventos/publicados")).andExpect(status().isOk());
        mvc.perform(get("/eventos/buscar")).andExpect(status().isOk());
        mvc.perform(get("/eventos/3")).andExpect(status().isOk());
        mvc.perform(get("/eventos/buscar").param("incluirNoPublicados", "true")).andExpect(status().isUnauthorized());
        mvc.perform(get("/eventos/buscar").param("incluirNoPublicados", "true").header("Authorization", bearer("CLIENTE")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/eventos/2").header("Authorization", bearer("ORGANIZADOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/eventos/1").header("Authorization", bearer("ORGANIZADOR"))).andExpect(status().isOk());
    }
    @Test void directAdministrativeRequestRequiresValidToken() throws Exception {
        mvc.perform(patch("/eventos/1/cancelar")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/eventos/1/cancelar").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
    }
}
