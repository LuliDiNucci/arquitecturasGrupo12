package com.ticketya.entradas.services.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class QrCodeServiceImplTest {

    private final QrCodeServiceImpl service = new QrCodeServiceImpl();

    @Test
    void generaImagenPngBase64Valida() throws Exception {
        String base64 = service.generarImagenBase64("entrada-1");

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(
                Base64.getDecoder().decode(base64)));

        assertNotNull(image);
        assertNotEquals(0, image.getWidth());
        assertNotEquals(0, image.getHeight());
    }

    @Test
    void contenidosDiferentesGeneranCodigosDiferentes() {
        String first = service.generarImagenBase64("entrada-1");
        String second = service.generarImagenBase64("entrada-2");

        assertNotEquals(first, second);
    }
}
