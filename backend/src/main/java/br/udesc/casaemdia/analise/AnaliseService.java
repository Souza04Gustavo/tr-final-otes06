package br.udesc.casaemdia.analise;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.casaemdia.analise.dto.AnaliseGastosResponse;
import br.udesc.casaemdia.analise.dto.GastoCategoriaResponse;
import br.udesc.casaemdia.analise.dto.GastoMensalResponse;
import br.udesc.casaemdia.analise.dto.MaiorGastoResponse;
import br.udesc.casaemdia.lembrete.Categoria;
import br.udesc.casaemdia.lembrete.Lembrete;
import br.udesc.casaemdia.lembrete.LembreteRepository;

/**
 * Análise de gastos: totais do mês, distribuição por categoria, maiores gastos e histórico (RN14 a RN17).
 * <p>
 * Entra na análise todo lembrete que tem valor, de qualquer categoria, no mês do seu vencimento.
 * O que está concluído conta como pago; o restante, como pendente.
 */
@Service
public class AnaliseService {

    static final int MESES_PADRAO = 6;
    static final int MESES_MINIMO = 2;
    static final int MESES_MAXIMO = 12;
    static final int TAMANHO_RANKING = 5;
    static final String ROTULO_OUTROS = "Outros";

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final LembreteRepository lembreteRepository;
    private final Clock clock;

    public AnaliseService(LembreteRepository lembreteRepository, Clock clock) {
        this.lembreteRepository = lembreteRepository;
        this.clock = clock;
    }

    /**
     * @param mes   mês analisado; {@code null} para o mês atual
     * @param meses tamanho do histórico, incluindo o mês analisado (limitado a 2..12)
     */
    @Transactional(readOnly = true)
    public AnaliseGastosResponse analisar(Long usuarioId, YearMonth mes, int meses) {
        YearMonth referencia = mes != null ? mes : YearMonth.now(clock);
        int tamanhoHistorico = Math.clamp(meses, MESES_MINIMO, MESES_MAXIMO);
        YearMonth primeiro = referencia.minusMonths(tamanhoHistorico - 1L);

        // Uma única consulta cobre todo o período; o agrupamento por mês é feito em memória
        Map<YearMonth, List<Lembrete>> porMes = new LinkedHashMap<>();
        for (YearMonth m = primeiro; !m.isAfter(referencia); m = m.plusMonths(1)) {
            porMes.put(m, new ArrayList<>());
        }
        lembreteRepository
                .findByUsuarioIdAndValorIsNotNullAndDataVencimentoBetween(
                        usuarioId, primeiro.atDay(1), referencia.atEndOfMonth())
                .forEach(l -> porMes.get(YearMonth.from(l.getDataVencimento())).add(l));

        List<GastoMensalResponse> historico = porMes.entrySet().stream()
                .map(e -> new GastoMensalResponse(e.getKey().toString(), somar(e.getValue(), false),
                        somar(e.getValue(), true)))
                .toList();

        List<Lembrete> doMes = porMes.get(referencia);
        BigDecimal total = somar(doMes, false);
        BigDecimal pago = somar(doMes, true);
        BigDecimal totalAnterior = historico.get(historico.size() - 2).total();

        return new AnaliseGastosResponse(
                referencia.toString(),
                total,
                pago,
                total.subtract(pago),
                doMes.size(),
                mediaDosMesesComGasto(historico),
                variacaoPercentual(total, totalAnterior),
                historico,
                distribuirPorCategoria(doMes, total),
                maioresGastos(doMes, total));
    }

    /** Soma os valores; com {@code somentePagos}, só os concluídos. */
    private static BigDecimal somar(List<Lembrete> lembretes, boolean somentePagos) {
        return lembretes.stream()
                .filter(l -> !somentePagos || l.isConcluido())
                .map(Lembrete::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** RN15: meses sem nenhum gasto (ex.: antes de o usuário começar a usar o app) não puxam a média para baixo. */
    private static BigDecimal mediaDosMesesComGasto(List<GastoMensalResponse> historico) {
        List<BigDecimal> totais = historico.stream()
                .map(GastoMensalResponse::total)
                .filter(t -> t.signum() > 0)
                .toList();
        if (totais.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return totais.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totais.size()), 2, RoundingMode.HALF_UP);
    }

    /** RN16: sem gasto no mês anterior não há base de comparação. */
    private static BigDecimal variacaoPercentual(BigDecimal total, BigDecimal anterior) {
        if (anterior.signum() == 0) {
            return null;
        }
        return total.subtract(anterior).multiply(CEM).divide(anterior, 1, RoundingMode.HALF_UP);
    }

    /** Categorias sem gasto ficam de fora; a ordem é sempre a do enum, para cada categoria manter sua cor no app. */
    private static List<GastoCategoriaResponse> distribuirPorCategoria(List<Lembrete> doMes, BigDecimal total) {
        Map<Categoria, BigDecimal> somas = new LinkedHashMap<>();
        for (Categoria categoria : Categoria.values()) {
            somas.put(categoria, BigDecimal.ZERO);
        }
        doMes.forEach(l -> somas.merge(l.getCategoria(), l.getValor(), BigDecimal::add));

        return somas.entrySet().stream()
                .filter(e -> e.getValue().signum() > 0)
                .map(e -> new GastoCategoriaResponse(e.getKey(), e.getValue(), percentual(e.getValue(), total)))
                .toList();
    }

    /**
     * RN17: soma os lembretes de mesmo título (sem diferenciar maiúsculas e espaços extras), ordena do maior
     * para o menor e junta o que passar do ranking em uma linha "Outros".
     */
    private static List<MaiorGastoResponse> maioresGastos(List<Lembrete> doMes, BigDecimal total) {
        Map<String, String> tituloExibido = new LinkedHashMap<>();
        Map<String, BigDecimal> somas = new LinkedHashMap<>();
        for (Lembrete lembrete : doMes) {
            String chave = lembrete.getTitulo().trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
            tituloExibido.putIfAbsent(chave, lembrete.getTitulo().trim());
            somas.merge(chave, lembrete.getValor(), BigDecimal::add);
        }

        List<Map.Entry<String, BigDecimal>> ordenados = somas.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();

        List<MaiorGastoResponse> ranking = new ArrayList<>();
        BigDecimal restante = BigDecimal.ZERO;
        for (int i = 0; i < ordenados.size(); i++) {
            Map.Entry<String, BigDecimal> entrada = ordenados.get(i);
            // Com exatamente TAMANHO_RANKING + 1 títulos, mostra todos em vez de um "Outros" com um só item
            boolean cabeNoRanking = i < TAMANHO_RANKING || ordenados.size() == TAMANHO_RANKING + 1;
            if (cabeNoRanking) {
                ranking.add(new MaiorGastoResponse(tituloExibido.get(entrada.getKey()), entrada.getValue(),
                        percentual(entrada.getValue(), total), false));
            } else {
                restante = restante.add(entrada.getValue());
            }
        }
        if (restante.signum() > 0) {
            ranking.add(new MaiorGastoResponse(ROTULO_OUTROS, restante, percentual(restante, total), true));
        }
        return ranking;
    }

    private static int percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return 0;
        }
        return parte.multiply(CEM).divide(total, 0, RoundingMode.HALF_UP).intValue();
    }
}
