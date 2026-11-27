package br.udesc.casaemdia.ui.lembretes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.udesc.casaemdia.R;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.data.model.LembreteRequest;
import br.udesc.casaemdia.data.repository.LembreteRepository;
import br.udesc.casaemdia.data.repository.ResultadoCallback;
import br.udesc.casaemdia.util.Resource;

/**
 * Carrega, salva e exclui um lembrete.
 */
public class LembreteFormViewModel extends AndroidViewModel {

    private final LembreteRepository repository = new LembreteRepository();
    private final MutableLiveData<Resource<LembreteDto>> lembrete = new MutableLiveData<>();
    /** Resultado de salvar/excluir; em caso de sucesso traz a mensagem para o usuário. */
    private final MutableLiveData<Resource<String>> operacao = new MutableLiveData<>();

    private boolean formularioPreenchido;

    public LembreteFormViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Garante que os campos sejam preenchidos com os dados do servidor uma única vez,
     * preservando o que o usuário editou ao girar a tela.
     *
     * @return {@code true} apenas na primeira chamada
     */
    public boolean devePreencherFormulario() {
        if (formularioPreenchido) {
            return false;
        }
        formularioPreenchido = true;
        return true;
    }

    public LiveData<Resource<LembreteDto>> getLembrete() {
        return lembrete;
    }

    public LiveData<Resource<String>> getOperacao() {
        return operacao;
    }

    /** Evita salvar ou excluir duas vezes com toques repetidos. */
    private boolean operacaoEmAndamento() {
        Resource<String> atual = operacao.getValue();
        return atual != null && atual.isCarregando();
    }

    /** Carrega o lembrete apenas uma vez (não recarrega ao girar a tela). */
    public void carregar(long id) {
        if (lembrete.getValue() != null) {
            return;
        }
        lembrete.setValue(Resource.carregando());
        repository.buscar(id, new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable LembreteDto dados) {
                lembrete.setValue(Resource.sucesso(dados));
            }

            @Override
            public void erro(String mensagem) {
                lembrete.setValue(Resource.erro(mensagem));
            }
        });
    }

    /** @param id {@code null} para criar um novo lembrete */
    public void salvar(@Nullable Long id, LembreteRequest requisicao) {
        if (operacaoEmAndamento()) {
            return;
        }
        operacao.setValue(Resource.carregando());
        repository.salvar(id, requisicao, new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable LembreteDto dados) {
                operacao.setValue(Resource.sucesso(getApplication().getString(R.string.lembrete_salvo)));
            }

            @Override
            public void erro(String mensagem) {
                operacao.setValue(Resource.erro(mensagem));
            }
        });
    }

    public void excluir(long id) {
        if (operacaoEmAndamento()) {
            return;
        }
        operacao.setValue(Resource.carregando());
        repository.excluir(id, new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable Void dados) {
                operacao.setValue(Resource.sucesso(getApplication().getString(R.string.lembrete_excluido)));
            }

            @Override
            public void erro(String mensagem) {
                operacao.setValue(Resource.erro(mensagem));
            }
        });
    }
}
