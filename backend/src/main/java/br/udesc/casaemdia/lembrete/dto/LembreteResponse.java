package br.udesc.casaemdia.lembrete.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.udesc.casaemdia.lembrete.Categoria;
import br.udesc.casaemdia.lembrete.Lembrete;
import br.udesc.casaemdia.lembrete.Recorrencia;

public record LembreteResponse(
        Long id,
        String titulo,
        Categoria categoria,
        LocalDate dataVencimento,
        BigDecimal valor,
        Recorrencia recorrencia,
        Integer intervaloDias,
        String observacao,
        boolean concluido,
        LocalDate dataConclusao,
        boolean atrasado) {

    /**
     * @param hoje data de referência para calcular se o lembrete está atrasado (RN07)
     */
    public static LembreteResponse from(Lembrete lembrete, LocalDate hoje) {
        return new LembreteResponse(
                lembrete.getId(),
                lembrete.getTitulo(),
                lembrete.getCategoria(),
                lembrete.getDataVencimento(),
                lembrete.getValor(),
                lembrete.getRecorrencia(),
                lembrete.getIntervaloDias(),
                lembrete.getObservacao(),
                lembrete.isConcluido(),
                lembrete.getDataConclusao(),
                lembrete.isAtrasado(hoje));
    }
}
