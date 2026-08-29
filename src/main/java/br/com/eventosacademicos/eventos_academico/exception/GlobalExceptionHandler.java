package br.com.eventosacademicos.eventos_academico.exception;

import br.com.eventosacademicos.eventos_academico.dto.response.ErrorResponseDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUsuarioNotFound(
            UsuarioNotFoundException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleEmptyResult(
            EmptyResultDataAccessException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado",
                request
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return build(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(
            Exception ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado",
                request
        );
    }

    private ResponseEntity<ErrorResponseDto> build(
            HttpStatus status,
            String message,
            WebRequest request
    ) {
        ErrorResponseDto body = new ErrorResponseDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false)
                        .replace("uri=", "")
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}