package br.udesc.casaemdia.data.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import br.udesc.casaemdia.data.session.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adiciona o header {@code Authorization: Bearer <token>} nas rotas autenticadas.
 * Se a API responder 401 (token expirado/inválido), encerra a sessão para o app voltar ao login.
 */
public class AuthInterceptor implements Interceptor {

    private static final String PREFIXO_ROTAS_PUBLICAS = "/api/auth/";

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request requisicao = chain.request();
        String token = sessionManager.getToken();
        boolean autenticada = token != null
                && !requisicao.url().encodedPath().startsWith(PREFIXO_ROTAS_PUBLICAS);

        if (autenticada) {
            requisicao = requisicao.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }

        Response resposta = chain.proceed(requisicao);
        if (autenticada && resposta.code() == 401) {
            sessionManager.expirar();
        }
        return resposta;
    }
}
