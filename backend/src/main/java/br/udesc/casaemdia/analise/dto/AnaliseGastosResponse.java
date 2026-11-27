package br.udesc.casaemdia.analise.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Análise dos gastos de um mês, com o histórico dos meses anteriores (RN14 a RN17).
 *
 * @param mes                 mês analisado, no formato {@code yyyy-MM}
 * @param total               soma dos lembretes com valor e vencimento no mês
 * @param pago                parte do total já concluída
 * @param pendente            parte do total ainda não concluída
 * @param quantidadeLancamentos quantos lembretes com valor vencem no mês
 * @param mediaMensal         média dos totais do histórico, considerando só os meses com gasto
 * @param variacaoPercentual  variação do total em relação ao mês anterior, em %; {@code null} se o
 *                            mês anterior não teve gasto
 * @param historico           totais mês a mês, do mais antigo até o mês analisado
 * @param porCategoria        distribuição do total do mês por categoria, sempre na ordem das categorias
 * @param maioresGastos       maiores gastos do mês agrupados por título, do maior para o menor
 */
public record AnaliseGastosResponse(
        String mes,
        BigDecimal total,
        BigDecimal pago,
        BigDecimal pendente,
        int quantidadeLancamentos,
        BigDecimal mediaMensal,
        BigDecimal variacaoPercentual,
        List<GastoMensalResponse> historico,
        List<GastoCategoriaResponse> porCategoria,
        List<MaiorGastoResponse> maioresGastos) {
}
