package com.ticketya.msvc_fila_virtual;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class MsvcFilaVirtualApplicationTests {
    @org.springframework.test.context.DynamicPropertySource
    static void key(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("ticketya.jwt.public-key", () -> com.ticketya.msvc_fila_virtual.security.TestTokens.PUBLIC);
    }

	@Test
	void contextLoads() {
	}

}
