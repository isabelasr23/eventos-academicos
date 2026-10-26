package br.com.eventosacademicos.eventos_academico.exception;

import br.com.eventosacademicos.eventos_academico.dto.response.ErrorResponseDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    @ExceptionHandler(CategoriaNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoriaNotFound(
            CategoriaNotFoundException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(NomeCategoriaAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleNomeCategoriaAlreadyExists(
            NomeCategoriaAlreadyExistsException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEventoNotFound(
            EventoNotFoundException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(PeriodoEventoInvalidoException.class)
    public ResponseEntity<ErrorResponseDto> handlePeriodoEventoInvalido(
            PeriodoEventoInvalidoException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(CategoriaEmUsoException.class)
    public ResponseEntity<ErrorResponseDto> handleCategoriaEmUso(
            CategoriaEmUsoException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(InscricaoNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleInscricaoNotFound(
            InscricaoNotFoundException ex,
            WebRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InscricaoNaoPermitidaException.class)
    public ResponseEntity<ErrorResponseDto> handleInscricaoNaoPermitida(
            InscricaoNaoPermitidaException ex,
            WebRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler({
            InscricaoInvalidaException.class,
            EventoEmUsoException.class,
            UsuarioEmUsoException.class,
            CapacidadeEventoInvalidaException.class
    })
    public ResponseEntity<ErrorResponseDto> handleConflitoInscricao(
            RuntimeException ex,
            WebRequest request
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos",
                request
        );
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponseDto> handleRequisicaoInvalida(
            Exception ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                "Corpo da requisição ou parâmetro inválido",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMetodoNaoPermitido(
            HttpRequestMethodNotSupportedException ex,
            WebRequest request
    ) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRotaNaoEncontrada(
            NoResourceFoundException ex,
            WebRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "Rota não encontrada", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleIntegridadeDados(
            DataIntegrityViolationException ex,
            WebRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "Operação viola um vínculo ou registro único do banco",
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