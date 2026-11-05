
package com.ticketya.entradas.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

    /*
     * Genera una imagen PNG con el codigo QR
     * correspondiente a una entrada.
     *
     * Recibe el codigo unico de la entrada
     * y devuelve la imagen codificada en Base64.
     */
    public String generarImagenBase64(String contenido) {

        try {

            // Crear una matriz que representa el codigo QR.
            BitMatrix matrix = new QRCodeWriter().encode(
                contenido,
                BarcodeFormat.QR_CODE,
                260,
                260
            );

            // Crear la imagen que almacenara el QR.
            BufferedImage imagen = new BufferedImage(
                matrix.getWidth(),
                matrix.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            // Recorrer la matriz y dibujar los pixeles.
            for (int y = 0; y < matrix.getHeight(); y++) {

                for (int x = 0; x < matrix.getWidth(); x++) {

                    // Negro si la posicion esta activa.
                    // Blanco si la posicion esta vacia.
                    imagen.setRGB(
                        x,
                        y,
                        matrix.get(x, y)
                            ? 0xFF000000
                            : 0xFFFFFFFF
                    );
                }
            }

            // Convertir la imagen PNG en bytes.
            try (ByteArrayOutputStream salida =
                    new ByteArrayOutputStream()) {

                if (!ImageIO.write(imagen, "PNG", salida)) {

                    throw new IllegalStateException(
                        "No se dispone de un codificador PNG"
                    );
                }

                // Convertir los bytes a una cadena Base64.
                return Base64.getEncoder()
                        .encodeToString(salida.toByteArray());
            }

        } catch (WriterException | IOException ex) {

            throw new IllegalStateException(
                "No se pudo generar el codigo QR",
                ex
            );
        }
    }
}
