package br.udesc.casaemdia.lembrete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraRecorrenciaTest {

    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 11, 10);
    private static final LocalDate CONCLUSAO = LocalDate.of(2026, 11, 26);

    private final CalculadoraRecorrencia calculadora = new CalculadoraRecorrencia();

    @Test
    @DisplayName("NENHUMA (pontual) não gera próxima ocorrência")
    void nenhumaNaoGeraProxima() {
        assertThat(calculadora.proximaData(Recorrencia.NENHUMA, VENCIMENTO, CONCLUSAO, null)).isEmpty();
    }

    @Test
    @DisplayName("SEMANAL soma 7 dias ao vencimento")
    void semanalSomaSeteDiasAoVencimento() {
        assertThat(calculadora.proximaData(Recorrencia.SEMANAL, VENCIMENTO, CONCLUSAO, null))
                .contains(LocalDate.of(2026, 11, 17));
    }

    @Test
    @DisplayName("MENSAL soma 1 mês ao vencimento, mesmo concluída com atraso")
    void mensalSomaUmMesAoVencimento() {
        assertThat(calculadora.proximaData(Recorrencia.MENSAL, VENCIMENTO, CONCLUSAO, null))
                .contains(LocalDate.of(2026, 12, 10));
    }

    @Test
    @DisplayName("MENSAL em 31/01 vai para o último dia de fevereiro (RN12)")
    void mensalAjustaParaUltimoDiaDoMes() {
        LocalDate trintaEUmDeJaneiro = LocalDate.of(2027, 1, 31);
        assertThat(calculadora.proximaData(Recorrencia.MENSAL, trintaEUmDeJaneiro, trintaEUmDeJaneiro, null))
                .contains(LocalDate.of(2027, 2, 28));
    }

    @Test
    @DisplayName("ANUAL soma 1 ano ao vencimento")
    void anualSomaUmAno() {
        assertThat(calculadora.proximaData(Recorrencia.ANUAL, VENCIMENTO, CONCLUSAO, null))
                .contains(LocalDate.of(2027, 11, 10));
    }

    @Test
    @DisplayName("A_CADA_N_DIAS conta a partir da data de conclusão, não do vencimento")
    void aCadaNDiasContaDaConclusao() {
        assertThat(calculadora.proximaData(Recorrencia.A_CADA_N_DIAS, VENCIMENTO, CONCLUSAO, 180))
                .contains(CONCLUSAO.plusDays(180));
    }

    @Test
    @DisplayName("A_CADA_N_DIAS sem intervalo é erro de programação")
    void aCadaNDiasSemIntervalo() {
        assertThatNullPointerException()
                .isThrownBy(() -> calculadora.proximaData(Recorrencia.A_CADA_N_DIAS, VENCIMENTO, CONCLUSAO, null));
    }
}
