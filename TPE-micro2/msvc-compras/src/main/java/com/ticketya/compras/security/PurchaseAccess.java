package com.ticketya.compras.security;

import com.ticketya.compras.repositories.CompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component("purchaseAccess")
public class PurchaseAccess {
    private final CompraRepository compras;
    public PurchaseAccess(CompraRepository compras) { this.compras = compras; }
    public boolean client(Long id) { return CurrentIdentity.owner(id); }
    public boolean purchase(Long id) {
        if (CurrentIdentity.admin() || CurrentIdentity.authority("SERVICE_entradas")) return true;
        return ownedPurchase(id);
    }
    public boolean ownedPurchase(Long id) {
        return CurrentIdentity.owner(compras.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso inexistente")).getClienteId());
    }
}
