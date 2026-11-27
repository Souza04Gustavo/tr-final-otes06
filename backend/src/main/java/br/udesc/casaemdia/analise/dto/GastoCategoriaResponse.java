package br.udesc.casaemdia.analise.dto;

import java.math.BigDecimal;

import br.udesc.casaemdia.lembrete.Categoria;

/**
 * Fatia do total do mês que pertence a uma categoria.
 *
 * @param categoria  categoria dos lembretes
 * @param valor      soma dos valores da categoria no mês
 * @param percentual participação no total do mês (0 a 100, arredondado)
 */
public record GastoCategoriaResponse(Categoria categoria, BigDecimal valor, int percentual) {
}
