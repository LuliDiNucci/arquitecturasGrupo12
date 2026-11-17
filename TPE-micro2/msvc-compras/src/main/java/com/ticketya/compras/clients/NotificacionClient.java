package com.ticketya.compras.clients;

import com.ticketya.compras.dtos.CompraConfirmadaNotificacionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificacionesClient", url = "${msvc.notificaciones.url}")
public interface NotificacionClient {
    @PostMapping("/notificaciones/compras-confirmadas")
    void avisarCompraConfirmada(@RequestBody CompraConfirmadaNotificacionDTO solicitud);
}
