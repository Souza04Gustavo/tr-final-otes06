package br.udesc.casaemdia.data.repository;

import br.udesc.casaemdia.data.api.ApiClient;
import br.udesc.casaemdia.data.api.AuthApi;
import br.udesc.casaemdia.data.model.AuthResponse;
import br.udesc.casaemdia.data.model.CadastroRequest;
import br.udesc.casaemdia.data.model.LoginRequest;

public class AuthRepository {

    private final AuthApi api = ApiClient.auth();

    public void login(String email, String senha, ResultadoCallback<AuthResponse> callback) {
        api.login(new LoginRequest(email, senha)).enqueue(new CallbackApi<>(callback));
    }

    public void cadastrar(CadastroRequest requisicao, ResultadoCallback<AuthResponse> callback) {
        api.cadastrar(requisicao).enqueue(new CallbackApi<>(callback));
    }
}
