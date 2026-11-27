package br.udesc.casaemdia.data.repository;

import java.util.List;

import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.api.CompraApi;
import br.udesc.casaemdia.data.model.ItemCompraDto;
import br.udesc.casaemdia.data.model.ItemCompraRequest;

public class CompraRepository {

    private final CompraApi api = ApiClient.compras();

    public void listar(ResultadoCallback<List<ItemCompraDto>> callback) {
        api.listar().enqueue(new CallbackApi<>(callback));
    }

    public void adicionar(String nome, int quantidade, ResultadoCallback<ItemCompraDto> callback) {
        api.criar(new ItemCompraRequest(nome, quantidade)).enqueue(new CallbackApi<>(callback));
    }

    public void editar(long id, String nome, int quantidade, ResultadoCallback<ItemCompraDto> callback) {
        api.atualizar(id, new ItemCompraRequest(nome, quantidade)).enqueue(new CallbackApi<>(callback));
    }

    public void alternar(long id, ResultadoCallback<ItemCompraDto> callback) {
        api.alternar(id).enqueue(new CallbackApi<>(callback));
    }

    public void excluir(long id, ResultadoCallback<Void> callback) {
        api.excluir(id).enqueue(new CallbackApi<>(callback));
    }

    public void limparComprados(ResultadoCallback<Void> callback) {
        api.limparComprados().enqueue(new CallbackApi<>(callback));
    }
}
