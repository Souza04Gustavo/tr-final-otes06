package br.udesc.casaemdia.data.repository;

import androidx.annotation.Nullable;

import java.util.List;

import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.api.LembreteApi;
import br.udesc.casaemdia.data.model.Categoria;
import br.udesc.casaemdia.data.model.ConclusaoDto;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.data.model.LembreteRequest;

public class LembreteRepository {

    private final LembreteApi api = ApiClient.lembretes();

    public void listar(@Nullable Categoria categoria, boolean concluido, ResultadoCallback<List<LembreteDto>> callback) {
        api.listar(categoria, concluido).enqueue(new CallbackApi<>(callback));
    }

    public void buscar(long id, ResultadoCallback<LembreteDto> callback) {
        api.buscar(id).enqueue(new CallbackApi<>(callback));
    }

    /** Cria quando {@code id} é nulo; caso contrário, atualiza. */
    public void salvar(@Nullable Long id, LembreteRequest requisicao, ResultadoCallback<LembreteDto> callback) {
        if (id == null) {
            api.criar(requisicao).enqueue(new CallbackApi<>(callback));
        } else {
            api.atualizar(id, requisicao).enqueue(new CallbackApi<>(callback));
        }
    }

    public void excluir(long id, ResultadoCallback<Void> callback) {
        api.excluir(id).enqueue(new CallbackApi<>(callback));
    }

    public void concluir(long id, ResultadoCallback<ConclusaoDto> callback) {
        api.concluir(id).enqueue(new CallbackApi<>(callback));
    }

    public void reabrir(long id, ResultadoCallback<LembreteDto> callback) {
        api.reabrir(id).enqueue(new CallbackApi<>(callback));
    }
}
