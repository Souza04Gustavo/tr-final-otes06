package br.udesc.casaemdia.data.repository;

import java.time.YearMonth;

import br.udesc.casaemdia.data.api.AnaliseApi;
import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.model.AnaliseGastosDto;

public class AnaliseRepository {

    private final AnaliseApi api = ApiClient.analises();

    public void gastos(YearMonth mes, int meses, ResultadoCallback<AnaliseGastosDto> callback) {
        api.gastos(mes.toString(), meses).enqueue(new CallbackApi<>(callback));
    }
}
