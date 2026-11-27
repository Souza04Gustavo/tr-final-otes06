package br.udesc.casaemdia.painel;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.casaemdia.compra.ItemCompraService;
import br.udesc.casaemdia.lembrete.Categoria;
import br.udesc.casaemdia.lembrete.Lembrete;
import br.udesc.casaemdia.lembrete.LembreteRepository;
import br.udesc.casaemdia.lembrete.dto.LembreteResponse;
import br.udesc.casaemdia.painel.dto.PainelResponse;
import br.udesc.casaemdia.painel.dto.ResumoMesResponse;
import br.udesc.casaemdia.usuario.UsuarioService;

/**
 * Monta o painel da casa a partir dos lembretes e da lista de compras (RN08, RN09).
 */
@Service
public class PainelService {

    static final int DIAS_A_FRENTE = 7;

    private final LembreteRepository lembreteRepository;
    private final ItemCompraService itemCompraService;
    private final UsuarioService usuarioService;
    private final Clock clock;

    public PainelService(LembreteRepository lembreteRepository, ItemCompraService itemCompraService,
            UsuarioService usuarioService, Clock clock) {
        this.lembreteRepository = lembreteRepository;
        this.itemCompraService = itemCompraService;
        this.usuarioService = usuarioService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PainelResponse montar(Long usuarioId) {
        LocalDate hoje = LocalDate.now(clock);
        String nomeCasa = usuarioService.buscarPorId(usuarioId).getNomeCasa();

        List<LembreteResponse> atrasados = new ArrayList<>();
        List<LembreteResponse> deHoje = new ArrayList<>();
        List<LembreteResponse> proximos = new ArrayList<>();

        // Uma única consulta: pendentes com vencimento até hoje + 7, já ordenados por data
        lembreteRepository
                .findByUsuarioIdAndConcluidoFalseAndDataVencimentoLessThanEqualOrderByDataVencimentoAscIdAsc(
                        usuarioId, hoje.plusDays(DIAS_A_FRENTE))
                .forEach(lembrete -> {
                    LembreteResponse resposta = LembreteResponse.from(lembrete, hoje);
                    LocalDate vencimento = lembrete.getDataVencimento();
                    if (vencimento.isBefore(hoje)) {
                        atrasados.add(resposta);
                    } else if (vencimento.isEqual(hoje)) {
                        deHoje.add(resposta);
                    } else {
                        proximos.add(resposta);
                    }
                });

        return new PainelResponse(
                nomeCasa,
                hoje,
                atrasados,
                deHoje,
                proximos,
                itemCompraService.contarPendentes(usuarioId),
                resumirContasDoMes(usuarioId, YearMonth.from(hoje)));
    }

    private ResumoMesResponse resumirContasDoMes(Long usuarioId, YearMonth mes) {
        List<Lembrete> contas = lembreteRepository.findByUsuarioIdAndCategoriaAndDataVencimentoBetween(
                usuarioId, Categoria.CONTA, mes.atDay(1), mes.atEndOfMonth());

        BigDecimal pago = BigDecimal.ZERO;
        BigDecimal pendente = BigDecimal.ZERO;
        for (Lembrete conta : contas) {
            BigDecimal valor = conta.getValor() == null ? BigDecimal.ZERO : conta.getValor();
            if (conta.isConcluido()) {
                pago = pago.add(valor);
            } else {
                pendente = pendente.add(valor);
            }
        }
        return new ResumoMesResponse(mes.toString(), pago.add(pendente), pago, pendente);
    }
}
