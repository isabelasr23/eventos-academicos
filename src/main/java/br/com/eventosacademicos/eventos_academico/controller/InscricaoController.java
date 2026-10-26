package br.com.eventosacademicos.eventos_academico.controller;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateInscricaoRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.InscricaoResponseDto;
import br.com.eventosacademicos.eventos_academico.dto.response.ErrorResponseDto;
import br.com.eventosacademicos.eventos_academico.mapper.InscricaoMapper;
import br.com.eventosacademicos.eventos_academico.model.Inscricao;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.InscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Inscrições", description = "Inscrição, consulta e cancelamento do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content)
@RestController
@RequestMapping("/inscricoes")
public class InscricaoController {

    private final InscricaoService inscricaoService;
    private final InscricaoMapper inscricaoMapper;

    public InscricaoController(
            InscricaoService inscricaoService,
            InscricaoMapper inscricaoMapper
    ) {
        this.inscricaoService = inscricaoService;
        this.inscricaoMapper = inscricaoMapper;
    }

    @Operation(summary = "Inscrever-se em um evento")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Inscrição realizada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da inscrição inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuário já inscrito, evento sem vagas ou inscrições encerradas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<InscricaoResponseDto> create(
            @RequestBody @Valid CreateInscricaoRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario
    ) {
        Inscricao inscricao = inscricaoMapper.toEntity(dto);

        Inscricao inscricaoCriada = inscricaoService.create(inscricao, usuario.getId());

        InscricaoResponseDto response = inscricaoMapper.toDto(inscricaoCriada);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Listar minhas inscrições")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de inscrições do usuário autenticado"
            )
    })
    @GetMapping
    public ResponseEntity<List<InscricaoResponseDto>> findAll(
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario
    ) {
        List<Inscricao> inscricoes = inscricaoService.findAll(usuario.getId());

        List<InscricaoResponseDto> response = inscricaoMapper.toDto(inscricoes);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar minha inscrição pelo ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscrição encontrada"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "A inscrição pertence a outro usuário",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inscrição não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<InscricaoResponseDto> findById(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario
    ) {
        Inscricao inscricao = inscricaoService.findById(id, usuario.getId());

        InscricaoResponseDto response = inscricaoMapper.toDto(inscricao);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar minha inscrição")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Inscrição cancelada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "A inscrição pertence a outro usuário",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inscrição não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario
    ) {
        inscricaoService.delete(id, usuario.getId());

        return ResponseEntity.noContent().build();
    }
}
