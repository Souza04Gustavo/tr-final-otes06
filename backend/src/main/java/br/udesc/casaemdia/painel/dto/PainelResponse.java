package br.udesc.casaemdia.painel.dto;

import java.time.LocalDate;
import java.util.List;

import br.udesc.casaemdia.lembrete.dto.LembreteResponse;

/**
 * Visão geral da casa exibida na tela inicial do app (RN09).
 */
public record PainelResponse(
        String nomeCasa,
        LocalDate dataReferencia,
        List<LembreteResponse> atrasados,
        List<LembreteResponse> hoje,
        List<LembreteResponse> proximos7Dias,
        long itensPendentesCompra,
        ResumoMesResponse resumoMes) {
}
