package br.udesc.casaemdia.analise.dto;

import java.math.BigDecimal;

/**
 * Total de um mês no histórico de gastos.
 *
 * @param mes   mês no formato {@code yyyy-MM}
 * @param total soma dos lembretes com valor e vencimento no mês
 * @param pago  parte do total já concluída
 */
public record GastoMensalResponse(String mes, BigDecimal total, BigDecimal pago) {
}
