package br.udesc.casaemdia.painel.dto;

import java.math.BigDecimal;

/**
 * Resumo das contas do mês (RN08).
 *
 * @param mes           mês de referência no formato {@code yyyy-MM}
 * @param totalContas   soma de todas as contas com vencimento no mês
 * @param totalPago     soma das contas concluídas
 * @param totalPendente soma das contas ainda não concluídas
 */
public record ResumoMesResponse(String mes, BigDecimal totalContas, BigDecimal totalPago, BigDecimal totalPendente) {
}
