package br.udesc.casaemdia.data.repository;

import androidx.annotation.NonNull;

import br.udesc.casaemdia.data.api.ErrosApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adapta o {@link Callback} do Retrofit para {@link ResultadoCallback},
 * convertendo respostas de erro e falhas de rede em mensagens.
 */
class CallbackApi<T> implements Callback<T> {

    private final ResultadoCallback<T> resultado;

    CallbackApi(ResultadoCallback<T> resultado) {
        this.resultado = resultado;
    }

    @Override
    public void onResponse(@NonNull Call<T> chamada, @NonNull Response<T> resposta) {
        if (resposta.isSuccessful()) {
            resultado.sucesso(resposta.body());
        } else {
            resultado.erro(ErrosApi.mensagem(resposta));
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> chamada, @NonNull Throwable falha) {
        if (!chamada.isCanceled()) {
            resultado.erro(ErrosApi.SEM_CONEXAO);
        }
    }
}
