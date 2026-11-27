package br.udesc.casaemdia.data.api;

import java.util.List;

import br.udesc.casaemdia.data.model.ItemCompraDto;
import br.udesc.casaemdia.data.model.ItemCompraRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CompraApi {

    @GET("api/compras")
    Call<List<ItemCompraDto>> listar();

    @POST("api/compras")
    Call<ItemCompraDto> criar(@Body ItemCompraRequest requisicao);

    @PUT("api/compras/{id}")
    Call<ItemCompraDto> atualizar(@Path("id") long id, @Body ItemCompraRequest requisicao);

    @PATCH("api/compras/{id}/alternar")
    Call<ItemCompraDto> alternar(@Path("id") long id);

    @DELETE("api/compras/{id}")
    Call<Void> excluir(@Path("id") long id);

    @DELETE("api/compras/comprados")
    Call<Void> limparComprados();
}
