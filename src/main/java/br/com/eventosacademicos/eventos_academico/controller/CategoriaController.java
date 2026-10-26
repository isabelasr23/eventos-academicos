package br.com.eventosacademicos.eventos_academico.controller;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateCategoriaRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateCategoriaRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.CategoriaResponseDto;
import br.com.eventosacademicos.eventos_academico.mapper.CategoriaMapper;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
import br.com.eventosacademicos.eventos_academico.dto.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categorias", description = "Operações relacionadas às categorias")
@SecurityRequirement(name = "bearerAuth")
@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content)
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;

    public CategoriaController(
            CategoriaService categoriaService,
            CategoriaMapper categoriaMapper
    ) {
        this.categoriaService = categoriaService;
        this.categoriaMapper = categoriaMapper;
    }


    //Create
    @Operation(summary = "Cadastrar uma categoria")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoria cadastrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de cadastro inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Nome de categoria já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CategoriaResponseDto> create(
            @RequestBody @Valid CreateCategoriaRequestDto dto
    ) {

        Categoria categoria = categoriaMapper.toEntity(dto);

        Categoria categoriaCriado = categoriaService.create(categoria);

        CategoriaResponseDto response = categoriaMapper.toDto(categoriaCriado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    //Listar todos
    @Operation(summary = "Listar todas as categorias")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorias retornada"
            )
    })
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDto>> findAll() {

        List<Categoria> categorias = categoriaService.findAll();

        List<CategoriaResponseDto> response = categoriaMapper.toDto(categorias);

        return ResponseEntity.ok(response);
    }

    //Listar apartir de um id
    @Operation(summary = "Buscar uma categoria pelo ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada"
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
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDto> findById(@PathVariable UUID id) {

        Categoria categoria = categoriaService.findById(id);

        CategoriaResponseDto response = categoriaMapper.toDto(categoria);

        return ResponseEntity.ok(response);
    }

    //Atualizar categoria usando id
    @Operation(summary = "Atualizar uma categoria")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria atualizada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de atualização inválidos",
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Nome já cadastrado em outra categoria",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDto> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCategoriaRequestDto dto
    ) {

        Categoria categoria = categoriaMapper.toEntity(dto);

        Categoria categoriaAtualizado = categoriaService.update(id, categoria);

        CategoriaResponseDto response = categoriaMapper.toDto(categoriaAtualizado);

        return ResponseEntity.ok(response);
    }

    //Deletar usando id
    @Operation(summary = "Excluir uma categoria")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoria excluída",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Categoria possui eventos vinculados",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        categoriaService.delete(id);

        return ResponseEntity.noContent().build();
    }


}