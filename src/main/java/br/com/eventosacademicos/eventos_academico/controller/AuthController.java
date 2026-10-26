package br.com.eventosacademicos.eventos_academico.controller;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.LoginRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.AuthResponseDto;
import br.com.eventosacademicos.eventos_academico.dto.response.ErrorResponseDto;
import br.com.eventosacademicos.eventos_academico.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Cadastro e login de usuários")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Autenticar um usuário", description = "Confere e-mail e senha e retorna um JWT válido por uma hora")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de login inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "E-mail ou senha inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody @Valid LoginRequestDto request
    ) {
        String token = authService.login(request);

        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    @Operation(summary = "Cadastrar um usuário", description = "Cria o usuário e retorna um JWT válido por uma hora")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de cadastro inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @RequestBody @Valid CreateUsuarioRequestDto request
    ) {
        String token = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDto(token));
    }
}
