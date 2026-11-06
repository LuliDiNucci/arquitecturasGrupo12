
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ticketya.compras.enums.EstadoPago;
import com.ticketya.compras.enums.EstadoReintegro;

@Component
public class PasarelaPagoMock {

        public record ResultadoPasarela(
                        EstadoPago estado,
                        String referenciaExterna) {
        }

        public ResultadoPasarela procesarPago(
                        BigDecimal importe,
                        String medioPago,
                        String claveIdempotencia) {

                if (importe == null
                                || importe.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                        "El importe del pago debe ser mayor a cero");
                }

                String referencia = "MOCK-" +
                                UUID.nameUUIDFromBytes(
                                                claveIdempotencia.getBytes(
                                                                StandardCharsets.UTF_8));

                if ("TARJETA_RECHAZADA".equals(medioPago)) {
                        return new ResultadoPasarela(
                                        EstadoPago.RECHAZADO,
                                        referencia);
                }

                return new ResultadoPasarela(
                                EstadoPago.APROBADO,
                                referencia);
        }

        public record ResultadoReintegro(EstadoReintegro estado, String referenciaExterna) {
        }

        public ResultadoReintegro reintegrar(
                        BigDecimal importe,
                        Long pagoId,
                        Long itemCompraId) {

                if (importe == null
                                || importe.compareTo(BigDecimal.ZERO) <= 0
                                || pagoId == null
                                || itemCompraId == null) {

                        throw new IllegalArgumentException(
                                        "Datos de reintegro inválidos");
                }

                String clave = "REINTEGRO-PAGO-" + pagoId
                                + "-ITEM-" + itemCompraId;

                String referencia = "MOCK-REINTEGRO-"
                                + UUID.nameUUIDFromBytes(
                                                clave.getBytes(StandardCharsets.UTF_8));

                return new ResultadoReintegro(
                                EstadoReintegro.APROBADO,
                                referencia);
        }

}
