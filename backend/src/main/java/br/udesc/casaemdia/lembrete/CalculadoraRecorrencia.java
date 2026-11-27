package br.udesc.casaemdia.lembrete;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Calcula a data da próxima ocorrência de um lembrete recorrente (RN05).
 * <p>
 * As recorrências de calendário (semanal, mensal, anual) partem da data de
 * <b>vencimento</b>, pois contas têm dia fixo. Já {@link Recorrencia#A_CADA_N_DIAS}
 * parte da data de <b>conclusão</b>: na manutenção importa quando o serviço foi feito.
 * <p>
 * Na recorrência mensal, se o dia não existir no mês seguinte, o Java ajusta para o
 * último dia do mês (31/01 → 28/02) — limitação aceita no MVP (RN12).
 */
@Component
public class CalculadoraRecorrencia {

    public Optional<LocalDate> proximaData(Recorrencia recorrencia, LocalDate dataVencimento,
            LocalDate dataConclusao, Integer intervaloDias) {
        Objects.requireNonNull(recorrencia, "recorrencia");
        Objects.requireNonNull(dataVencimento, "dataVencimento");

        return switch (recorrencia) {
            case NENHUMA -> Optional.empty();
            case SEMANAL -> Optional.of(dataVencimento.plusWeeks(1));
            case MENSAL -> Optional.of(dataVencimento.plusMonths(1));
            case ANUAL -> Optional.of(dataVencimento.plusYears(1));
            case A_CADA_N_DIAS -> {
                Objects.requireNonNull(dataConclusao, "dataConclusao");
                Objects.requireNonNull(intervaloDias, "intervaloDias");
                yield Optional.of(dataConclusao.plusDays(intervaloDias));
            }
        };
    }
}
