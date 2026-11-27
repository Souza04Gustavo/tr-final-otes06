package br.udesc.casaemdia.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.udesc.casaemdia.data.model.AuthResponse;
import br.udesc.casaemdia.data.model.CadastroRequest;
import br.udesc.casaemdia.data.repository.AuthRepository;
import br.udesc.casaemdia.data.repository.ResultadoCallback;
import br.udesc.casaemdia.data.session.SessionManager;
import br.udesc.casaemdia.util.Resource;

/**
 * Login e cadastro. Em caso de sucesso, a sessão é salva antes de avisar a tela.
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repository = new AuthRepository();
    private final SessionManager sessao;
    private final MutableLiveData<Resource<AuthResponse>> resultado = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        sessao = SessionManager.getInstance(application);
    }

    public LiveData<Resource<AuthResponse>> getResultado() {
        return resultado;
    }

    public void login(String email, String senha) {
        if (emAndamento()) {
            return;
        }
        resultado.setValue(Resource.carregando());
        repository.login(email, senha, aoAutenticar());
    }

    public void cadastrar(String nome, String nomeCasa, String email, String senha) {
        if (emAndamento()) {
            return;
        }
        resultado.setValue(Resource.carregando());
        repository.cadastrar(new CadastroRequest(nome, nomeCasa, email, senha), aoAutenticar());
    }

    /** Evita requisições duplicadas (ex.: toque duplo ou tecla Enter repetida). */
    private boolean emAndamento() {
        Resource<AuthResponse> atual = resultado.getValue();
        return atual != null && atual.isCarregando();
    }

    private ResultadoCallback<AuthResponse> aoAutenticar() {
        return new ResultadoCallback<>() {
            @Override
            public void sucesso(@Nullable AuthResponse dados) {
                if (dados == null) {
                    resultado.setValue(Resource.erro("Resposta vazia do servidor"));
                    return;
                }
                sessao.salvar(dados);
                resultado.setValue(Resource.sucesso(dados));
            }

            @Override
            public void erro(String mensagem) {
                resultado.setValue(Resource.erro(mensagem));
            }
        };
    }
}
