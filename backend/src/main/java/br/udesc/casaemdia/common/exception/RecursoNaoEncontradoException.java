package br.udesc.casaemdia.common.exception;

/**
 * Recurso inexistente ou pertencente a outro usuário (RN01). Resulta em HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
