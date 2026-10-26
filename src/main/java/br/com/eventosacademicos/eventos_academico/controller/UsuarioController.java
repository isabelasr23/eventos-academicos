package br.com.eventosacademicos.eventos_academico.controller;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.UsuarioResponseDto;
import br.com.eventosacademicos.eventos_academico.mapper.UsuarioMapper;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
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

@Tag(name = "Usuários", description = "Operações relacionadas aos usuários")
@SecurityRequirement(name = "bearerAuth")
@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado", content = @Content)
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }


    //Create
    @Operation(summary = "Cadastrar um usuário")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado"
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
                    description = "E-mail já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDto> create(
            @RequestBody @Valid CreateUsuarioRequestDto dto
    ) {

        Usuario usuario = usuarioMapper.toEntity(dto);

        Usuario usuarioCriado = usuarioService.create(usuario);

        UsuarioResponseDto response = usuarioMapper.toDto(usuarioCriado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    //Listar todos
    @Operation(summary = "Listar todos os usuários")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários retornada"
            )
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {

        List<Usuario> usuarios = usuarioService.findAll();

        List<UsuarioResponseDto> response = usuarioMapper.toDto(usuarios);

        return ResponseEntity.ok(response);
    }

    //Listar apartir de um id
    @Operation(summary = "Buscar um usuário pelo ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@PathVariable UUID id) {

        Usuario usuario = usuarioService.findById(id);

        UsuarioResponseDto response = usuarioMapper.toDto(usuario);

        return ResponseEntity.ok(response);
    }

    //Atualizar usuario usando id
    @Operation(summary = "Atualizar um usuário")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado"
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
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado por outro usuário",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUsuarioRequestDto dto
    ) {

        Usuario usuario = usuarioMapper.toEntity(dto);

        Usuario usuarioAtualizado = usuarioService.update(id, usuario);

        UsuarioResponseDto response = usuarioMapper.toDto(usuarioAtualizado);

        return ResponseEntity.ok(response);
    }

    //Deletar usando id
    @Operation(summary = "Excluir um usuário")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuário possui inscrições vinculadas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário excluído",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        usuarioService.delete(id);

        return ResponseEntity.noContent().build();
    }


}