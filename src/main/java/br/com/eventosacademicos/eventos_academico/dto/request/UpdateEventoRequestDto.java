package br.com.eventosacademicos.eventos_academico.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateEventoRequestDto(

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @NotNull(message = "O início é obrigatório")
        LocalDateTime inicio,

        @NotNull(message = "O fim é obrigatório")
        LocalDateTime fim,

        @NotNull(message = "A capacidade é obrigatória")
        @Min(value = 1, message = "A capacidade deve ser de pelo menos uma vaga")
        Integer capacidade,

        @NotNull(message = "A categoria é obrigatória")
        UUID categoriaId

) {}
