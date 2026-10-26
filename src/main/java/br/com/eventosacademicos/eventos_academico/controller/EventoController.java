package br.com.eventosacademicos.eventos_academico.controller;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateEventoRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateEventoRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.EventoResponseDto;
import br.com.eventosacademicos.eventos_academico.dto.response.ErrorResponseDto;
import br.com.eventosacademicos.eventos_academico.mapper.EventoMapper;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Eventos", description = "Operações relacionadas aos eventos acadêmicos")
@SecurityRequirement(name = "bearerAuth")
@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content)
@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final EventoMapper eventoMapper;

    public EventoController(
            EventoService eventoService,
            EventoMapper eventoMapper
    ) {
        this.eventoService = eventoService;
        this.eventoMapper = eventoMapper;
    }

    @Operation(summary = "Cadastrar um evento")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Evento cadastrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de cadastro ou período inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<EventoResponseDto> create(
            @RequestBody @Valid CreateEventoRequestDto dto
    ) {
        Evento evento = eventoMapper.toEntity(dto);

        Evento eventoCriado = eventoService.create(evento);

        EventoResponseDto response = eventoMapper.toDto(eventoCriado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Listar todos os eventos")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de eventos retornada"
            )
    })
    @GetMapping
    public ResponseEntity<List<EventoResponseDto>> findAll() {
        List<Evento> eventos = eventoService.findAll();

        List<EventoResponseDto> response = eventoMapper.toDto(eventos);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar um evento pelo ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDto> findById(@PathVariable UUID id) {
        Evento evento = eventoService.findById(id);

        EventoResponseDto response = eventoMapper.toDto(evento);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar um evento")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "409",
                    description = "Capacidade inferior ao total de inscritos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento atualizado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de atualização ou período inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento ou categoria não encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDto> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateEventoRequestDto dto
    ) {
        Evento evento = eventoMapper.toEntity(dto);

        Evento eventoAtualizado = eventoService.update(id, evento);

        EventoResponseDto response = eventoMapper.toDto(eventoAtualizado);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir um evento")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "409",
                    description = "Evento possui inscrições vinculadas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Evento excluído",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventoService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
