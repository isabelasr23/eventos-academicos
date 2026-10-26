package br.com.eventosacademicos.eventos_academico.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInscricaoRequestDto(

        @NotNull(message = "O evento é obrigatório")
        UUID eventoId

) {}
