package br.com.eventosacademicos.eventos_academico.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventoResponseDto(
        UUID id,
        String titulo,
        String descricao,
        LocalDateTime inicio,
        LocalDateTime fim,
        Integer capacidade,
        UUID categoriaId
) {
}
