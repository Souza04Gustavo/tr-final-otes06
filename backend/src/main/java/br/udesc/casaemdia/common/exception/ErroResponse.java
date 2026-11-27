package br.udesc.casaemdia.common.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato padrão de todas as respostas de erro da API (ver docs/03-api.md, seção 3.3).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoErro> campos) {

    /** Erro de validação de um campo específico. */
    public record CampoErro(String campo, String mensagem) {
    }

    public static ErroResponse of(LocalDateTime timestamp, HttpStatusCode status, String mensagem,
            String caminho, List<CampoErro> campos) {
        HttpStatus conhecido = HttpStatus.resolve(status.value());
        String erro = conhecido != null ? conhecido.getReasonPhrase() : String.valueOf(status.value());
        return new ErroResponse(timestamp, status.value(), erro, mensagem, caminho, campos);
    }
}
