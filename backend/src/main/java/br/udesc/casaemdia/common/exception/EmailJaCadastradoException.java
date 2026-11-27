package br.udesc.casaemdia.common.exception;

/**
 * Tentativa de cadastro com um e-mail já existente (RN02). Resulta em HTTP 409.
 */
public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Já existe uma conta com este e-mail");
    }
}
