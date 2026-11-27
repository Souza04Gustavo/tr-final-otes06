package br.udesc.casaemdia.lembrete;

/**
 * Como um lembrete se repete ao ser concluído. As regras de cálculo da próxima
 * data ficam em {@link CalculadoraRecorrencia}.
 */
public enum Recorrencia {
    /** Pontual: não gera próxima ocorrência. */
    NENHUMA,
    /** Data de vencimento + 7 dias. */
    SEMANAL,
    /** Data de vencimento + 1 mês. */
    MENSAL,
    /** Data de vencimento + 1 ano. */
    ANUAL,
    /** Data de conclusão + N dias (manutenção periódica). */
    A_CADA_N_DIAS
}
