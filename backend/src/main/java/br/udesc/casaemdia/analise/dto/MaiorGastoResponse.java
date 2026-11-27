package br.udesc.casaemdia.analise.dto;

import java.math.BigDecimal;

/**
 * Um dos maiores gastos do mês, somando os lembretes de mesmo título.
 *
 * @param titulo     título do lembrete (ou "Outros" quando {@code agrupado})
 * @param valor      soma dos valores com esse título no mês
 * @param percentual participação no total do mês (0 a 100, arredondado)
 * @param agrupado   {@code true} na linha que soma os gastos que ficaram fora do ranking
 */
public record MaiorGastoResponse(String titulo, BigDecimal valor, int percentual, boolean agrupado) {
}
