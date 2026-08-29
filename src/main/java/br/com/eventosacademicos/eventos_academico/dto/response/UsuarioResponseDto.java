package br.com.eventosacademicos.eventos_academico.dto.response;

import java.util.UUID;

public record UsuarioResponseDto(
        UUID id,
        String nome,
        String email
) {
}
