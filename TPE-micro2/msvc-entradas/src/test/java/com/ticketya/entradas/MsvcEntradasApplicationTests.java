package com.ticketya.entradas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:entradas_context;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class MsvcEntradasApplicationTests {

    @org.springframework.beans.factory.annotation.Autowired
    private com.ticketya.entradas.repositories.EntradaRepository repository;

    @org.springframework.test.context.DynamicPropertySource
    static void securityKeys(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> com.ticketya.entradas.security.TestTokens.PUBLIC);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mvc;

    @Test
    void ownershipIsEnforcedAgainstPersistedTicket() throws Exception {
        var ticket = new com.ticketya.entradas.entities.Entrada();
        ticket.setCompraId(1L); ticket.setReservaId(1L); ticket.setDetalleReservaId(1L);
        ticket.setIndiceLugar(1); ticket.setClienteId(7L); ticket.setEventoId(1L); ticket.setSectorId(1L);
        ticket.setCodigoQr(java.util.UUID.randomUUID().toString());
        ticket.setEstado("EMITIDA"); ticket.setFechaEmision(java.time.LocalDateTime.now());
        long id = repository.saveAndFlush(ticket).getId();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/entradas/{id}", id)
            .header("X-Cliente-Id", "7").header("Authorization", "Bearer " + com.ticketya.entradas.security.TestTokens.user(7, "CLIENTE")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.clienteId").value(7));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/entradas/{id}", id)
            .header("X-Cliente-Id", "7").header("Authorization", "Bearer " + com.ticketya.entradas.security.TestTokens.user(8, "CLIENTE")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

}
