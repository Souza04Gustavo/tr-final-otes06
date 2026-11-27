package br.udesc.casaemdia.common.exception;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.udesc.casaemdia.common.exception.ErroResponse.CampoErro;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Converte as exceções da aplicação no formato padrão {@link ErroResponse}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErro(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: verifique o JSON, as datas (yyyy-MM-dd) e os valores dos enums",
                request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'", request, null);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> tratarCredenciais(CredenciaisInvalidasException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> tratarEmailDuplicado(EmailJaCadastradoException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    /**
     * Demais exceções. As do próprio Spring MVC (rota inexistente, método não suportado,
     * parâmetro ausente...) já carregam o status correto; as outras viram 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarGenerica(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse erroSpring) {
            HttpStatusCode status = erroSpring.getStatusCode();
            return responder(status, mensagemParaErroDoSpring(status, erroSpring), request, null);
        }
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request, null);
    }

    private static String mensagemParaErroDoSpring(HttpStatusCode status, ErrorResponse erro) {
        return switch (status.value()) {
            case 404 -> "Rota não encontrada";
            case 405 -> "Método HTTP não suportado nesta rota";
            case 415 -> "Tipo de conteúdo não suportado: envie application/json";
            default -> erro.getBody().getDetail();
        };
    }

    private ResponseEntity<ErroResponse> responder(HttpStatusCode status, String mensagem,
            HttpServletRequest request, List<CampoErro> campos) {
        LocalDateTime agora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        ErroResponse corpo = ErroResponse.of(agora, status, mensagem, request.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
