package dev.skillforge.api;

import dev.skillforge.domain.InvalidSkillNameException;
import dev.skillforge.generation.LintFailedException;
import dev.skillforge.template.TemplateNotFoundException;
import dev.skillforge.validation.LimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

/**
 * Todas as respostas de erro no formato ProblemDetail (RFC 9457), com mensagens em português.
 * Erros inesperados viram 500 genérico: o stack trace fica só no log.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(LintFailedException.class)
    ProblemDetail lintFailed(LintFailedException e) {
        ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_CONTENT, "Biblioteca com erros de validação",
                e.getMessage() + " Corrija as violações ERROR e tente de novo.");
        problem.setProperty("violations", e.report().violations());
        return problem;
    }

    @ExceptionHandler(LimitExceededException.class)
    ProblemDetail limitExceeded(LimitExceededException e) {
        return problem(HttpStatus.CONTENT_TOO_LARGE, "Limite excedido", e.getMessage());
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    ProblemDetail templateNotFound(TemplateNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Modelo não encontrado", e.getMessage());
    }

    /** Inclui InvalidSkillNameException e base64 inválido. */
    @ExceptionHandler({InvalidSkillNameException.class, IllegalArgumentException.class})
    ProblemDetail badArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("Erro inesperado", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Algo deu errado do nosso lado. Tente de novo em instantes.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", String.valueOf(error.getDefaultMessage())))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Alguns campos não passaram na validação.");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "O corpo da requisição não é um JSON válido para este endpoint."));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
