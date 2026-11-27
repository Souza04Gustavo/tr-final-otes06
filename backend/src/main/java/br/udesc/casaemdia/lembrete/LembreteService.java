package br.udesc.casaemdia.lembrete;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.casaemdia.common.exception.RecursoNaoEncontradoException;
import br.udesc.casaemdia.common.exception.RegraNegocioException;
import br.udesc.casaemdia.lembrete.dto.ConclusaoResponse;
import br.udesc.casaemdia.lembrete.dto.LembreteRequest;
import br.udesc.casaemdia.lembrete.dto.LembreteResponse;
import br.udesc.casaemdia.usuario.Usuario;
import br.udesc.casaemdia.usuario.UsuarioRepository;

@Service
public class LembreteService {

    static final int INTERVALO_MINIMO_DIAS = 1;
    static final int INTERVALO_MAXIMO_DIAS = 3650;

    private final LembreteRepository lembreteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CalculadoraRecorrencia calculadoraRecorrencia;
    private final Clock clock;

    public LembreteService(LembreteRepository lembreteRepository, UsuarioRepository usuarioRepository,
            CalculadoraRecorrencia calculadoraRecorrencia, Clock clock) {
        this.lembreteRepository = lembreteRepository;
        this.usuarioRepository = usuarioRepository;
        this.calculadoraRecorrencia = calculadoraRecorrencia;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LembreteResponse> listar(Long usuarioId, Categoria categoria, boolean concluido) {
        List<Lembrete> lembretes = categoria == null
                ? lembreteRepository.findByUsuarioIdAndConcluidoOrderByDataVencimentoAscIdAsc(usuarioId, concluido)
                : lembreteRepository.findByUsuarioIdAndConcluidoAndCategoriaOrderByDataVencimentoAscIdAsc(
                        usuarioId, concluido, categoria);
        LocalDate hoje = hoje();
        return lembretes.stream().map(l -> LembreteResponse.from(l, hoje)).toList();
    }

    @Transactional(readOnly = true)
    public LembreteResponse buscar(Long usuarioId, Long id) {
        return LembreteResponse.from(buscarEntidade(usuarioId, id), hoje());
    }

    @Transactional
    public LembreteResponse criar(Long usuarioId, LembreteRequest request) {
        validarRegras(request);
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Lembrete lembrete = new Lembrete(
                usuario,
                request.titulo().trim(),
                request.categoria(),
                request.dataVencimento(),
                request.valor(),
                request.recorrencia(),
                intervaloNormalizado(request),
                textoOuNulo(request.observacao()),
                LocalDateTime.now(clock));
        return LembreteResponse.from(lembreteRepository.save(lembrete), hoje());
    }

    @Transactional
    public LembreteResponse atualizar(Long usuarioId, Long id, LembreteRequest request) {
        validarRegras(request);
        Lembrete lembrete = buscarEntidade(usuarioId, id);
        lembrete.atualizar(
                request.titulo().trim(),
                request.categoria(),
                request.dataVencimento(),
                request.valor(),
                request.recorrencia(),
                intervaloNormalizado(request),
                textoOuNulo(request.observacao()));
        return LembreteResponse.from(lembrete, hoje());
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        lembreteRepository.delete(buscarEntidade(usuarioId, id));
    }

    /**
     * RN05/RN06: marca como concluído hoje e, se for recorrente, cria a próxima ocorrência.
     */
    @Transactional
    public ConclusaoResponse concluir(Long usuarioId, Long id) {
        Lembrete lembrete = buscarEntidade(usuarioId, id);
        if (lembrete.isConcluido()) {
            throw new RegraNegocioException("Este lembrete já foi concluído");
        }

        LocalDate hoje = hoje();
        lembrete.concluir(hoje);

        Optional<LocalDate> proximaData = calculadoraRecorrencia.proximaData(
                lembrete.getRecorrencia(), lembrete.getDataVencimento(),
                lembrete.getDataConclusao(), lembrete.getIntervaloDias());

        LembreteResponse proximo = proximaData
                .map(data -> lembreteRepository.save(lembrete.novaOcorrencia(data, LocalDateTime.now(clock))))
                .map(novo -> LembreteResponse.from(novo, hoje))
                .orElse(null);

        return new ConclusaoResponse(LembreteResponse.from(lembrete, hoje), proximo);
    }

    /**
     * Desfaz a conclusão (botão "Desfazer" do app): o lembrete volta a ficar pendente e a próxima
     * ocorrência criada por ele é removida, desde que ainda esteja pendente.
     */
    @Transactional
    public LembreteResponse reabrir(Long usuarioId, Long id) {
        Lembrete lembrete = buscarEntidade(usuarioId, id);
        if (!lembrete.isConcluido()) {
            throw new RegraNegocioException("Este lembrete não está concluído");
        }
        lembreteRepository.deleteAll(lembreteRepository.findByUsuarioIdAndOrigemIdAndConcluidoFalse(usuarioId, id));
        lembrete.reabrir();
        return LembreteResponse.from(lembrete, hoje());
    }

    private Lembrete buscarEntidade(Long usuarioId, Long id) {
        return lembreteRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lembrete não encontrado"));
    }

    /** RN03 e RN04: regras que envolvem mais de um campo. */
    private static void validarRegras(LembreteRequest request) {
        if (request.categoria() == Categoria.CONTA && request.valor() == null) {
            throw new RegraNegocioException("Lembretes da categoria CONTA precisam ter um valor");
        }
        if (request.recorrencia() == Recorrencia.A_CADA_N_DIAS) {
            Integer intervalo = request.intervaloDias();
            if (intervalo == null || intervalo < INTERVALO_MINIMO_DIAS || intervalo > INTERVALO_MAXIMO_DIAS) {
                throw new RegraNegocioException("Para repetir a cada N dias, informe um intervalo entre "
                        + INTERVALO_MINIMO_DIAS + " e " + INTERVALO_MAXIMO_DIAS + " dias");
            }
        }
    }

    /** RN04: o intervalo só é gravado na recorrência A_CADA_N_DIAS. */
    private static Integer intervaloNormalizado(LembreteRequest request) {
        return request.recorrencia() == Recorrencia.A_CADA_N_DIAS ? request.intervaloDias() : null;
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }
}
