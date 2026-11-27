package br.udesc.casaemdia.analise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.udesc.casaemdia.analise.dto.AnaliseGastosResponse;
import br.udesc.casaemdia.analise.dto.GastoCategoriaResponse;
import br.udesc.casaemdia.analise.dto.GastoMensalResponse;
import br.udesc.casaemdia.analise.dto.MaiorGastoResponse;
import br.udesc.casaemdia.lembrete.Categoria;
import br.udesc.casaemdia.lembrete.Lembrete;
import br.udesc.casaemdia.lembrete.LembreteRepository;
import br.udesc.casaemdia.lembrete.Recorrencia;
import br.udesc.casaemdia.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
class AnaliseServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 26);
    private static final Long USUARIO_ID = 1L;

    @Mock
    private LembreteRepository lembreteRepository;

    private AnaliseService service;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        Clock relogioFixo = Clock.fixed(HOJE.atTime(10, 0).atZone(FUSO).toInstant(), FUSO);
        service = new AnaliseService(lembreteRepository, relogioFixo);
        usuario = new Usuario("Gustavo", "Casa da Família Souza", "gustavo@email.com", "hash",
                LocalDateTime.of(2026, 1, 1, 9, 0));
    }

    @Test
    @DisplayName("Totais do mês, distribuição por categoria e variação em relação ao mês anterior")
    void totaisDoMes() {
        dadosDoPeriodo(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 11, 30), List.of(
                lembrete("Aluguel", Categoria.CONTA, LocalDate.of(2026, 10, 10), "1200.00", true),
                lembrete("Aluguel", Categoria.CONTA, LocalDate.of(2026, 11, 10), "1200.00", true),
                lembrete("Água", Categoria.CONTA, LocalDate.of(2026, 11, 15), "86.30", true),
                lembrete("Conta de luz", Categoria.CONTA, LocalDate.of(2026, 11, 25), "184.50", false),
                lembrete("Limpar calhas", Categoria.MANUTENCAO, LocalDate.of(2026, 11, 20), "150.00", false),
                lembrete("Presente", Categoria.OUTRO, LocalDate.of(2026, 11, 5), "79.20", true)));

        AnaliseGastosResponse analise = service.analisar(USUARIO_ID, null, 6);

        assertThat(analise.mes()).isEqualTo("2026-11");
        assertThat(analise.total()).isEqualByComparingTo("1700.00");
        assertThat(analise.pago()).isEqualByComparingTo("1365.50");
        assertThat(analise.pendente()).isEqualByComparingTo("334.50");
        assertThat(analise.quantidadeLancamentos()).isEqualTo(5);
        // (1700 - 1200) / 1200 = 41,7%
        assertThat(analise.variacaoPercentual()).isEqualByComparingTo("41.7");

        assertThat(analise.porCategoria()).extracting(GastoCategoriaResponse::categoria)
                .containsExactly(Categoria.CONTA, Categoria.MANUTENCAO, Categoria.OUTRO);
        assertThat(analise.porCategoria()).extracting(GastoCategoriaResponse::percentual)
                .containsExactly(87, 9, 5); // 1470,80 / 150,00 / 79,20 de 1700,00
    }

    @Test
    @DisplayName("Histórico tem um item por mês, inclusive meses sem gasto, e a média ignora os meses vazios (RN15)")
    void historicoEMedia() {
        dadosDoPeriodo(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 11, 30), List.of(
                lembrete("Aluguel", Categoria.CONTA, LocalDate.of(2026, 9, 10), "1000.00", true),
                lembrete("Aluguel", Categoria.CONTA, LocalDate.of(2026, 10, 10), "1200.00", true),
                lembrete("Aluguel", Categoria.CONTA, LocalDate.of(2026, 11, 10), "1400.00", false)));

        AnaliseGastosResponse analise = service.analisar(USUARIO_ID, YearMonth.of(2026, 11), 6);

        assertThat(analise.historico()).extracting(GastoMensalResponse::mes)
                .containsExactly("2026-06", "2026-07", "2026-08", "2026-09", "2026-10", "2026-11");
        assertThat(analise.historico().get(0).total()).isEqualByComparingTo("0");
        assertThat(analise.historico().get(4).pago()).isEqualByComparingTo("1200.00");
        assertThat(analise.historico().get(5).pago()).isEqualByComparingTo("0");
        assertThat(analise.mediaMensal()).isEqualByComparingTo("1200.00");
    }

    @Test
    @DisplayName("Maiores gastos somam títulos iguais e agrupam o excedente em \"Outros\" (RN17)")
    void maioresGastos() {
        List<Lembrete> lembretes = new ArrayList<>(List.of(
                lembrete("Mercado", Categoria.OUTRO, LocalDate.of(2026, 11, 3), "300.00", true),
                lembrete("  mercado ", Categoria.OUTRO, LocalDate.of(2026, 11, 17), "250.00", true),
                lembrete("Aluguel", Categoria.CONTA, LocalDate.of(2026, 11, 10), "1200.00", true)));
        String[] outros = {"Luz", "Água", "Internet", "Gás", "Celular"};
        for (int i = 0; i < outros.length; i++) {
            lembretes.add(lembrete(outros[i], Categoria.CONTA, LocalDate.of(2026, 11, 20), (100 - i * 10) + ".00",
                    false));
        }
        dadosDoPeriodo(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 11, 30), lembretes);

        AnaliseGastosResponse analise = service.analisar(USUARIO_ID, YearMonth.of(2026, 11), 6);

        assertThat(analise.maioresGastos()).extracting(MaiorGastoResponse::titulo)
                .containsExactly("Aluguel", "Mercado", "Luz", "Água", "Internet", "Outros");
        assertThat(analise.maioresGastos().get(1).valor()).isEqualByComparingTo("550.00");
        MaiorGastoResponse agrupado = analise.maioresGastos().getLast();
        assertThat(agrupado.agrupado()).isTrue();
        assertThat(agrupado.valor()).isEqualByComparingTo("130.00"); // Gás (70) + Celular (60)
    }

    @Test
    @DisplayName("Mês sem gastos: totais zerados, sem variação e sem distribuição")
    void mesSemGastos() {
        when(lembreteRepository.findByUsuarioIdAndValorIsNotNullAndDataVencimentoBetween(
                eq(USUARIO_ID), any(), any())).thenReturn(List.of());

        AnaliseGastosResponse analise = service.analisar(USUARIO_ID, null, 99);

        assertThat(analise.total()).isEqualByComparingTo("0");
        assertThat(analise.variacaoPercentual()).isNull();
        assertThat(analise.mediaMensal()).isEqualByComparingTo("0");
        assertThat(analise.porCategoria()).isEmpty();
        assertThat(analise.maioresGastos()).isEmpty();
        // O tamanho do histórico é limitado a 12 meses
        assertThat(analise.historico()).hasSize(AnaliseService.MESES_MAXIMO);
    }

    private void dadosDoPeriodo(LocalDate inicio, LocalDate fim, List<Lembrete> lembretes) {
        when(lembreteRepository.findByUsuarioIdAndValorIsNotNullAndDataVencimentoBetween(USUARIO_ID, inicio, fim))
                .thenReturn(lembretes);
    }

    private Lembrete lembrete(String titulo, Categoria categoria, LocalDate vencimento, String valor,
            boolean concluido) {
        Lembrete lembrete = new Lembrete(usuario, titulo, categoria, vencimento, new BigDecimal(valor),
                Recorrencia.NENHUMA, null, null, LocalDateTime.of(2026, 1, 1, 9, 0));
        if (concluido) {
            lembrete.concluir(vencimento);
        }
        return lembrete;
    }
}
