package br.udesc.casaemdia.ui.lembretes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.Categoria;
import br.udesc.casaemdia.data.model.ConclusaoDto;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.data.repository.LembreteRepository;
import br.udesc.casaemdia.data.repository.ResultadoCallback;
import br.udesc.casaemdia.util.Aviso;
import br.udesc.casaemdia.util.Busca;
import br.udesc.casaemdia.util.Evento;
import br.udesc.casaemdia.util.Formatadores;
import br.udesc.casaemdia.util.Resource;

/**
 * Lista de lembretes com filtros por categoria e situação, busca por texto e a ação de concluir
 * (que pode ser desfeita).
 * <p>
 * Categoria e situação são filtradas pela API; a busca é feita aqui, sobre a lista já carregada,
 * para responder a cada letra digitada sem novas requisições.
 */
public class LembretesViewModel extends AndroidViewModel {

    private final LembreteRepository repository = new LembreteRepository();
    private final MutableLiveData<Resource<List<LembreteDto>>> lembretes = new MutableLiveData<>();
    private final MutableLiveData<Evento<Aviso>> avisos = new MutableLiveData<>();

    @Nullable
    private Categoria categoria;
    private boolean concluidos;
    private String busca = "";
    /** Última lista recebida da API, antes da busca. */
    private List<LembreteDto> carregados = Collections.emptyList();

    public LembretesViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<List<LembreteDto>>> getLembretes() {
        return lembretes;
    }

    public LiveData<Evento<Aviso>> getAvisos() {
        return avisos;
    }

    public boolean isConcluidos() {
        return concluidos;
    }

    public String getBusca() {
        return busca;
    }

    /** @param categoria {@code null} para todas */
    public void filtrar(@Nullable Categoria categoria, boolean concluidos) {
        this.categoria = categoria;
        this.concluidos = concluidos;
        carregar();
    }

    /** Aplica a busca sobre a lista já carregada. */
    public void buscar(@Nullable String termo) {
        String novo = termo == null ? "" : termo;
        if (novo.equals(busca)) {
            return;
        }
        busca = novo;
        Resource<List<LembreteDto>> atual = lembretes.getValue();
        if (atual != null && atual.getStatus() == Resource.Status.SUCESSO) {
            lembretes.setValue(Resource.sucesso(aplicarBusca()));
        }
    }

    public void carregar() {
        lembretes.setValue(Resource.carregando());
        repository.listar(categoria, concluidos, new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable List<LembreteDto> dados) {
                carregados = dados == null ? Collections.emptyList() : dados;
                lembretes.setValue(Resource.sucesso(aplicarBusca()));
            }

            @Override
            public void erro(String mensagem) {
                lembretes.setValue(Resource.erro(mensagem));
            }
        });
    }

    public void concluir(LembreteDto lembrete) {
        repository.concluir(lembrete.getId(), new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable ConclusaoDto dados) {
                Application app = getApplication();
                String texto = dados != null && dados.getProximo() != null
                        ? app.getString(R.string.lembrete_concluido_proximo,
                                Formatadores.dataCompleta(dados.getProximo().getDataVencimento()))
                        : app.getString(R.string.lembrete_concluido);
                avisos.setValue(new Evento<>(Aviso.comDesfazer(texto, () -> reabrir(lembrete.getId()))));
                carregar();
            }

            @Override
            public void erro(String mensagem) {
                avisos.setValue(new Evento<>(Aviso.simples(mensagem)));
            }
        });
    }

    /** "Desfazer" da conclusão: o lembrete volta a pendente e a próxima ocorrência gerada é removida. */
    private void reabrir(long id) {
        repository.reabrir(id, new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable LembreteDto dados) {
                avisos.setValue(new Evento<>(Aviso.simples(getApplication().getString(R.string.conclusao_desfeita))));
                carregar();
            }

            @Override
            public void erro(String mensagem) {
                avisos.setValue(new Evento<>(Aviso.simples(mensagem)));
            }
        });
    }

    private List<LembreteDto> aplicarBusca() {
        if (Busca.normalizar(busca).isEmpty()) {
            return carregados;
        }
        List<LembreteDto> encontrados = new ArrayList<>();
        for (LembreteDto lembrete : carregados) {
            if (Busca.corresponde(busca, lembrete.getTitulo(), lembrete.getObservacao())) {
                encontrados.add(lembrete);
            }
        }
        return encontrados;
    }
}
