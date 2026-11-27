package br.udesc.casaemdia.painel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.udesc.casaemdia.compra.ItemCompraService;
import br.udesc.casaemdia.lembrete.Categoria;
import br.udesc.casaemdia.lembrete.Lembrete;
import br.udesc.casaemdia.lembrete.LembreteRepository;
import br.udesc.casaemdia.lembrete.Recorrencia;
import br.udesc.casaemdia.lembrete.dto.LembreteResponse;
import br.udesc.casaemdia.painel.dto.PainelResponse;
import br.udesc.casaemdia.usuario.Usuario;
import br.udesc.casaemdia.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class PainelServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 26);
    private static final Long USUARIO_ID = 1L;

    @Mock
    private LembreteRepository lembreteRepository;

    @Mock
    private ItemCompraService itemCompraService;

    @Mock
    private UsuarioService usuarioService;

    private PainelService service;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        Clock relogioFixo = Clock.fixed(HOJE.atTime(10, 0).atZone(FUSO).toInstant(), FUSO);
        service = new PainelService(lembreteRepository, itemCompraService, usuarioService, relogioFixo);
        usuario = new Usuario("Gustavo", "Casa da Família Souza", "gustavo@email.com", "hash",
                LocalDateTime.of(2026, 11, 1, 9, 0));
        when(usuarioService.buscarPorId(USUARIO_ID)).thenReturn(usuario);
    }

    @Test
    @DisplayName("Separa os pendentes em atrasados, hoje e próximos 7 dias (RN09)")
    void classificaPendentes() {
        Lembrete luz = conta("Conta de luz", LocalDate.of(2026, 11, 25), "184.50", false);
        Lembrete lixo = lembrete("Lixo reciclável", Categoria.OUTRO, HOJE, null, false);
        Lembrete internet = conta("Internet", LocalDate.of(2026, 11, 28), "99.90", false);
        Lembrete condominio = conta("Condomínio", HOJE.plusDays(7), "450.00", false);
        when(lembreteRepository
                .findByUsuarioIdAndConcluidoFalseAndDataVencimentoLessThanEqualOrderByDataVencimentoAscIdAsc(
                        USUARIO_ID, HOJE.plusDays(7)))
                .thenReturn(List.of(luz, lixo, internet, condominio));
        when(itemCompraService.contarPendentes(USUARIO_ID)).thenReturn(4L);

        PainelResponse painel = service.montar(USUARIO_ID);

        assertThat(painel.nomeCasa()).isEqualTo("Casa da Família Souza");
        assertThat(painel.dataReferencia()).isEqualTo(HOJE);
        assertThat(painel.atrasados()).extracting(LembreteResponse::titulo).containsExactly("Conta de luz");
        assertThat(painel.atrasados().getFirst().atrasado()).isTrue();
        assertThat(painel.hoje()).extracting(LembreteResponse::titulo).containsExactly("Lixo reciclável");
        assertThat(painel.proximos7Dias()).extracting(LembreteResponse::titulo)
                .containsExactly("Internet", "Condomínio");
        assertThat(painel.itensPendentesCompra()).isEqualTo(4);
    }

    @Test
    @DisplayName("Resumo do mês soma contas pagas e pendentes com vencimento no mês (RN08)")
    void resumeContasDoMes() {
        when(lembreteRepository.findByUsuarioIdAndCategoriaAndDataVencimentoBetween(
                eq(USUARIO_ID), eq(Categoria.CONTA), eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 30))))
                .thenReturn(List.of(
                        conta("Aluguel", LocalDate.of(2026, 11, 10), "1200.00", true),
                        conta("Água", LocalDate.of(2026, 11, 15), "86.30", true),
                        conta("Conta de luz", LocalDate.of(2026, 11, 25), "184.50", false),
                        conta("Internet", LocalDate.of(2026, 11, 28), "99.90", false)));

        PainelResponse painel = service.montar(USUARIO_ID);

        assertThat(painel.resumoMes().mes()).isEqualTo("2026-11");
        assertThat(painel.resumoMes().totalContas()).isEqualByComparingTo("1570.70");
        assertThat(painel.resumoMes().totalPago()).isEqualByComparingTo("1286.30");
        assertThat(painel.resumoMes().totalPendente()).isEqualByComparingTo("284.40");
    }

    @Test
    @DisplayName("Painel vazio retorna listas vazias e totais zerados")
    void painelVazio() {
        PainelResponse painel = service.montar(USUARIO_ID);

        assertThat(painel.atrasados()).isEmpty();
        assertThat(painel.hoje()).isEmpty();
        assertThat(painel.proximos7Dias()).isEmpty();
        assertThat(painel.resumoMes().totalContas()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Lembrete conta(String titulo, LocalDate vencimento, String valor, boolean concluido) {
        return lembrete(titulo, Categoria.CONTA, vencimento, new BigDecimal(valor), concluido);
    }

    private Lembrete lembrete(String titulo, Categoria categoria, LocalDate vencimento, BigDecimal valor,
            boolean concluido) {
        Lembrete lembrete = new Lembrete(usuario, titulo, categoria, vencimento, valor, Recorrencia.MENSAL, null,
                null, LocalDateTime.of(2026, 11, 1, 9, 0));
        if (concluido) {
            lembrete.concluir(vencimento);
        }
        return lembrete;
    }
}
