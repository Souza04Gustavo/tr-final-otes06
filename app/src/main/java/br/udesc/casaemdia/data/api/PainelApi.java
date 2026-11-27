package br.udesc.casaemdia.data.api;

import br.udesc.casaemdia.data.model.PainelDto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface PainelApi {

    @GET("api/painel")
    Call<PainelDto> painel();
}
