package br.udesc.casaemdia.data.api;

import java.util.List;

import br.udesc.casaemdia.data.model.Categoria;
import br.udesc.casaemdia.data.model.ConclusaoDto;
import br.udesc.casaemdia.data.model.LembreteDto;
import br.udesc.casaemdia.data.model.LembreteRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LembreteApi {

    /** @param categoria {@code null} para todas as categorias */
    @GET("api/lembretes")
    Call<List<LembreteDto>> listar(@Query("categoria") Categoria categoria, @Query("concluido") boolean concluido);

    @GET("api/lembretes/{id}")
    Call<LembreteDto> buscar(@Path("id") long id);

    @POST("api/lembretes")
    Call<LembreteDto> criar(@Body LembreteRequest requisicao);

    @PUT("api/lembretes/{id}")
    Call<LembreteDto> atualizar(@Path("id") long id, @Body LembreteRequest requisicao);

    @DELETE("api/lembretes/{id}")
    Call<Void> excluir(@Path("id") long id);

    @PATCH("api/lembretes/{id}/concluir")
    Call<ConclusaoDto> concluir(@Path("id") long id);

    /** Desfaz a conclusão e remove a próxima ocorrência gerada por ela. */
    @PATCH("api/lembretes/{id}/reabrir")
    Call<LembreteDto> reabrir(@Path("id") long id);
}
