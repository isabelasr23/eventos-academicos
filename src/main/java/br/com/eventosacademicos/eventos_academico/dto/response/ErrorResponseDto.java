package br.com.eventosacademicos.eventos_academico.dto.response;

import java.time.Instant;

public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
