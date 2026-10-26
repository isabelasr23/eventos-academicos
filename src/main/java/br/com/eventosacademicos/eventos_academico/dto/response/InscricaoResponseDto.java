package br.com.eventosacademicos.eventos_academico.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record InscricaoResponseDto(
        UUID id,
        UUID usuarioId,
        UUID eventoId,
        LocalDateTime dataInscricao
) {
}
