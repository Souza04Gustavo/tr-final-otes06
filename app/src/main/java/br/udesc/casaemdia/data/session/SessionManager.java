package br.udesc.casaemdia.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import br.udesc.casaemdia.data.model.AuthResponse;
import br.udesc.casaemdia.util.Evento;

/**
 * Guarda a sessão do usuário (token JWT e dados básicos) nas SharedPreferences,
 * mantendo o login entre aberturas do app (RF02).
 */
public final class SessionManager {

    private static final String ARQUIVO = "sessao";
    private static final String CHAVE_TOKEN = "token";
    private static final String CHAVE_EXPIRA_EM = "expira_em";
    private static final String CHAVE_NOME = "nome";
    private static final String CHAVE_NOME_CASA = "nome_casa";
    private static final String CHAVE_EMAIL = "email";

    private static volatile SessionManager instancia;

    private final SharedPreferences preferencias;
    private final MutableLiveData<Evento<Boolean>> sessaoExpirada = new MutableLiveData<>();

    private SessionManager(Context context) {
        preferencias = context.getApplicationContext().getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instancia == null) {
            synchronized (SessionManager.class) {
                if (instancia == null) {
                    instancia = new SessionManager(context);
                }
            }
        }
        return instancia;
    }

    public void salvar(AuthResponse resposta) {
        preferencias.edit()
                .putString(CHAVE_TOKEN, resposta.getToken())
                .putString(CHAVE_EXPIRA_EM, resposta.getExpiraEm() == null ? null : resposta.getExpiraEm().toString())
                .putString(CHAVE_NOME, resposta.getUsuario().getNome())
                .putString(CHAVE_NOME_CASA, resposta.getUsuario().getNomeCasa())
                .putString(CHAVE_EMAIL, resposta.getUsuario().getEmail())
                .apply();
    }

    /** Há token salvo e ele ainda não expirou. */
    public boolean estaLogado() {
        String token = getToken();
        if (token == null) {
            return false;
        }
        String expiraEm = preferencias.getString(CHAVE_EXPIRA_EM, null);
        if (expiraEm == null) {
            return true;
        }
        try {
            return LocalDateTime.now().isBefore(LocalDateTime.parse(expiraEm));
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    @Nullable
    public String getToken() {
        return preferencias.getString(CHAVE_TOKEN, null);
    }

    public String getNome() {
        return preferencias.getString(CHAVE_NOME, "");
    }

    public String getNomeCasa() {
        return preferencias.getString(CHAVE_NOME_CASA, "");
    }

    public String getEmail() {
        return preferencias.getString(CHAVE_EMAIL, "");
    }

    /** Logout feito pelo usuário. */
    public void encerrar() {
        preferencias.edit().clear().apply();
    }

    /**
     * Chamado quando a API responde 401 (token expirado ou inválido).
     * Pode ser chamado fora da thread principal.
     */
    public void expirar() {
        if (getToken() == null) {
            return;
        }
        encerrar();
        sessaoExpirada.postValue(new Evento<>(true));
    }

    /** Observado pelas telas autenticadas para voltar ao login. */
    public LiveData<Evento<Boolean>> getSessaoExpirada() {
        return sessaoExpirada;
    }
}
