package br.udesc.casaemdia.data.api;

import com.google.gson.JsonParseException;

import java.util.List;

import br.udesc.casaemdia.data.model.ErroResponse;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Traduz respostas de erro da API em mensagens para o usuário.
 */
public final class ErrosApi {

    public static final String SEM_CONEXAO =
            "Não foi possível conectar ao servidor. Verifique se o back-end está rodando "
                    + "e se o app tem permissão de acesso à rede local.";

    private ErrosApi() {
    }

    /** Lê o {@link ErroResponse} do corpo da resposta e monta uma mensagem legível. */
    public static String mensagem(Response<?> resposta) {
        String padrao = "Erro inesperado (HTTP " + resposta.code() + ")";
        try (ResponseBody corpo = resposta.errorBody()) {
            if (corpo == null) {
                return padrao;
            }
            ErroResponse erro = ApiClient.gson().fromJson(corpo.charStream(), ErroResponse.class);
            if (erro == null) {
                return padrao;
            }
            List<ErroResponse.CampoErro> campos = erro.getCampos();
            if (campos != null && !campos.isEmpty()) {
                StringBuilder texto = new StringBuilder();
                for (ErroResponse.CampoErro campo : campos) {
                    if (texto.length() > 0) {
                        texto.append('\n');
                    }
                    texto.append(campo.getCampo()).append(": ").append(campo.getMensagem());
                }
                return texto.toString();
            }
            return erro.getMensagem() != null ? erro.getMensagem() : padrao;
        } catch (JsonParseException e) {
            // Corpo não era um ErroResponse válido (o Gson também converte falhas de leitura nesta exceção)
            return padrao;
        }
    }
}
