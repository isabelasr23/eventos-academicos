package br.com.eventosacademicos.eventos_academico.dto.response;

import java.util.UUID;

public record CategoriaResponseDto(
        UUID id,
        String nome
) {
}
