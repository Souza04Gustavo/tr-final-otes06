package br.udesc.casaemdia.lembrete.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.udesc.casaemdia.lembrete.Categoria;
import br.udesc.casaemdia.lembrete.Recorrencia;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados para criar ou editar um lembrete. As regras que dependem de mais de um
 * campo (RN03, RN04) são validadas no {@code LembreteService}.
 */
public record LembreteRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String titulo,

        @NotNull(message = "é obrigatória")
        Categoria categoria,

        @NotNull(message = "é obrigatória")
        LocalDate dataVencimento,

        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        @NotNull(message = "é obrigatória")
        Recorrencia recorrencia,

        Integer intervaloDias,

        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String observacao) {
}
