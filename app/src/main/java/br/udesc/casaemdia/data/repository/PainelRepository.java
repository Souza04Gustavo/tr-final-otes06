package br.udesc.casaemdia.data.repository;

import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.api.PainelApi;
import br.udesc.casaemdia.data.model.PainelDto;

public class PainelRepository {

    private final PainelApi api = ApiClient.painel();

    public void carregar(ResultadoCallback<PainelDto> callback) {
        api.painel().enqueue(new CallbackApi<>(callback));
    }
}
