package br.udesc.casaemdia.lembrete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.udesc.casaemdia.common.exception.RecursoNaoEncontradoException;
import br.udesc.casaemdia.common.exception.RegraNegocioException;
import br.udesc.casaemdia.lembrete.dto.ConclusaoResponse;
import br.udesc.casaemdia.lembrete.dto.LembreteRequest;
import br.udesc.casaemdia.lembrete.dto.LembreteResponse;
import br.udesc.casaemdia.usuario.Usuario;
import br.udesc.casaemdia.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class LembreteServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 26);
    private static final Long USUARIO_ID = 1L;
    private static final Long LEMBRETE_ID = 7L;

    @Mock
    private LembreteRepository lembreteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private LembreteService service;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        Clock relogioFixo = Clock.fixed(HOJE.atTime(10, 0).atZone(FUSO).toInstant(), FUSO);
        service = new LembreteService(lembreteRepository, usuarioRepository, new CalculadoraRecorrencia(),
                relogioFixo);
        usuario = new Usuario("Gustavo", "Casa da Família Souza", "gustavo@email.com", "hash",
                LocalDateTime.of(2026, 11, 1, 9, 0));
    }

    @Test
    @DisplayName("Concluir conta mensal cria a próxima ocorrência um mês depois do vencimento")
    void concluirRecorrenteCriaProximaOcorrencia() {
        Lembrete contaDeLuz = lembrete("Conta de luz", Categoria.CONTA, LocalDate.of(2026, 11, 25),
                new BigDecimal("184.50"), Recorrencia.MENSAL, null);
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.of(contaDeLuz));
        when(lembreteRepository.save(any(Lembrete.class))).thenAnswer(inv -> inv.getArgument(0));

        ConclusaoResponse resposta = service.concluir(USUARIO_ID, LEMBRETE_ID);

        assertThat(resposta.concluido().concluido()).isTrue();
        assertThat(resposta.concluido().dataConclusao()).isEqualTo(HOJE);
        assertThat(resposta.proximo()).isNotNull();
        assertThat(resposta.proximo().dataVencimento()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(resposta.proximo().concluido()).isFalse();
        assertThat(resposta.proximo().titulo()).isEqualTo("Conta de luz");
        assertThat(resposta.proximo().valor()).isEqualByComparingTo("184.50");
    }

    @Test
    @DisplayName("Concluir manutenção periódica agenda a próxima a partir de hoje + N dias")
    void concluirManutencaoPeriodicaContaDaConclusao() {
        Lembrete filtro = lembrete("Trocar filtro da água", Categoria.MANUTENCAO, LocalDate.of(2026, 11, 1),
                null, Recorrencia.A_CADA_N_DIAS, 180);
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.of(filtro));
        when(lembreteRepository.save(any(Lembrete.class))).thenAnswer(inv -> inv.getArgument(0));

        ConclusaoResponse resposta = service.concluir(USUARIO_ID, LEMBRETE_ID);

        assertThat(resposta.proximo().dataVencimento()).isEqualTo(HOJE.plusDays(180));
    }

    @Test
    @DisplayName("Concluir lembrete pontual não cria próxima ocorrência")
    void concluirPontualNaoCriaProxima() {
        Lembrete torneira = lembrete("Consertar torneira", Categoria.MANUTENCAO, HOJE, null,
                Recorrencia.NENHUMA, null);
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.of(torneira));

        ConclusaoResponse resposta = service.concluir(USUARIO_ID, LEMBRETE_ID);

        assertThat(resposta.concluido().concluido()).isTrue();
        assertThat(resposta.proximo()).isNull();
        verify(lembreteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Concluir duas vezes viola a RN06")
    void concluirDuasVezesFalha() {
        Lembrete jaConcluido = lembrete("Internet", Categoria.CONTA, HOJE, BigDecimal.TEN,
                Recorrencia.MENSAL, null);
        jaConcluido.concluir(HOJE);
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.of(jaConcluido));

        assertThatThrownBy(() -> service.concluir(USUARIO_ID, LEMBRETE_ID))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Desfazer a conclusão volta a pendente e remove a próxima ocorrência gerada")
    void reabrirDesfazConclusao() {
        Lembrete contaDeLuz = lembrete("Conta de luz", Categoria.CONTA, LocalDate.of(2026, 11, 25),
                new BigDecimal("184.50"), Recorrencia.MENSAL, null);
        contaDeLuz.concluir(HOJE);
        Lembrete proximaOcorrencia = contaDeLuz.novaOcorrencia(LocalDate.of(2026, 12, 25), LocalDateTime.now());
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.of(contaDeLuz));
        when(lembreteRepository.findByUsuarioIdAndOrigemIdAndConcluidoFalse(USUARIO_ID, LEMBRETE_ID))
                .thenReturn(List.of(proximaOcorrencia));

        LembreteResponse resposta = service.reabrir(USUARIO_ID, LEMBRETE_ID);

        assertThat(resposta.concluido()).isFalse();
        assertThat(resposta.dataConclusao()).isNull();
        verify(lembreteRepository).deleteAll(List.of(proximaOcorrencia));
    }

    @Test
    @DisplayName("Não é possível desfazer a conclusão de um lembrete pendente")
    void reabrirPendenteFalha() {
        Lembrete pendente = lembrete("Internet", Categoria.CONTA, HOJE, BigDecimal.TEN, Recorrencia.MENSAL, null);
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.of(pendente));

        assertThatThrownBy(() -> service.reabrir(USUARIO_ID, LEMBRETE_ID))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não está concluído");
    }

    @Test
    @DisplayName("Lembrete de outro usuário é tratado como inexistente (RN01)")
    void lembreteDeOutroUsuarioNaoEncontrado() {
        when(lembreteRepository.findByIdAndUsuarioId(LEMBRETE_ID, USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.concluir(USUARIO_ID, LEMBRETE_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Conta sem valor viola a RN03")
    void contaSemValorFalha() {
        LembreteRequest request = new LembreteRequest("Água", Categoria.CONTA, HOJE, null,
                Recorrencia.MENSAL, null, null);

        assertThatThrownBy(() -> service.criar(USUARIO_ID, request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("valor");
    }

    @Test
    @DisplayName("A cada N dias sem intervalo viola a RN04")
    void aCadaNDiasSemIntervaloFalha() {
        LembreteRequest request = new LembreteRequest("Dedetização", Categoria.MANUTENCAO, HOJE, null,
                Recorrencia.A_CADA_N_DIAS, null, null);

        assertThatThrownBy(() -> service.criar(USUARIO_ID, request))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Intervalo em dias é descartado quando a recorrência não é A_CADA_N_DIAS (RN04)")
    void intervaloIgnoradoForaDeACadaNDias() {
        when(usuarioRepository.getReferenceById(USUARIO_ID)).thenReturn(usuario);
        when(lembreteRepository.save(any(Lembrete.class))).thenAnswer(inv -> inv.getArgument(0));
        LembreteRequest request = new LembreteRequest("Aluguel", Categoria.CONTA, HOJE,
                new BigDecimal("1200.00"), Recorrencia.MENSAL, 30, "  ");

        var criado = service.criar(USUARIO_ID, request);

        assertThat(criado.intervaloDias()).isNull();
        assertThat(criado.observacao()).isNull();
    }

    private Lembrete lembrete(String titulo, Categoria categoria, LocalDate vencimento, BigDecimal valor,
            Recorrencia recorrencia, Integer intervaloDias) {
        return new Lembrete(usuario, titulo, categoria, vencimento, valor, recorrencia, intervaloDias, null,
                LocalDateTime.of(2026, 11, 1, 9, 0));
    }
}
