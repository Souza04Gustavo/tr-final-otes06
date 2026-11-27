package br.udesc.casaemdia.lembrete;

/**
 * Categoria do lembrete.
 * <ul>
 * <li>{@link #CONTA}: valor obrigatório; entra no resumo financeiro do mês.</li>
 * <li>{@link #MANUTENCAO}: pontual ou periódica (normalmente {@link Recorrencia#A_CADA_N_DIAS}).</li>
 * <li>{@link #OUTRO}: demais compromissos da casa.</li>
 * </ul>
 */
public enum Categoria {
    CONTA,
    MANUTENCAO,
    OUTRO
}
