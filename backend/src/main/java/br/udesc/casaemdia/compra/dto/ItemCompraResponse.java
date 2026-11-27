package br.udesc.casaemdia.compra.dto;

import java.time.LocalDateTime;

import br.udesc.casaemdia.compra.ItemCompra;

public record ItemCompraResponse(Long id, String nome, int quantidade, boolean comprado, LocalDateTime criadoEm) {

    public static ItemCompraResponse from(ItemCompra item) {
        return new ItemCompraResponse(item.getId(), item.getNome(), item.getQuantidade(), item.isComprado(),
                item.getCriadoEm());
    }
}
