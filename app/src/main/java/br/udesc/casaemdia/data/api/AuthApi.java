package br.udesc.casaemdia.data.api;

import br.udesc.casaemdia.data.model.AuthResponse;
import br.udesc.casaemdia.data.model.CadastroRequest;
import br.udesc.casaemdia.data.model.LoginRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest requisicao);

    @POST("api/auth/cadastro")
    Call<AuthResponse> cadastrar(@Body CadastroRequest requisicao);
}
