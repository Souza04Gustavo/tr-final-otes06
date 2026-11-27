package br.udesc.casaemdia.common.exception;

/**
 * Violação de uma regra de negócio (ex.: conta sem valor, concluir duas vezes). Resulta em HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
