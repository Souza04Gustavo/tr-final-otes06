package br.udesc.casaemdia.common.exception;

/**
 * E-mail ou senha incorretos no login. Resulta em HTTP 401.
 * A mensagem é a mesma nos dois casos para não revelar quais e-mails existem.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
