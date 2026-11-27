package br.udesc.casaemdia.compra.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para adicionar ou editar um item (RN10). Sem quantidade, assume 1.
 */
public record ItemCompraRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 80, message = "deve ter no máximo 80 caracteres")
        String nome,

        @Min(value = 1, message = "deve ser no mínimo 1")
        @Max(value = 9999, message = "deve ser no máximo 9999")
        Integer quantidade) {

    public int quantidadeOuPadrao() {
        return quantidade == null ? 1 : quantidade;
    }
}
