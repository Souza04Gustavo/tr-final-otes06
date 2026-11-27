package br.udesc.casaemdia.data.api;

import br.udesc.casaemdia.data.model.AnaliseGastosDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AnaliseApi {

    /**
     * @param mes   mês analisado no formato yyyy-MM
     * @param meses quantos meses o histórico deve ter, incluindo o analisado
     */
    @GET("api/analises/gastos")
    Call<AnaliseGastosDto> gastos(@Query("mes") String mes, @Query("meses") int meses);
}
