package br.com.eventosacademicos.eventos_academico.controller;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.UsuarioResponseDto;
import br.com.eventosacademicos.eventos_academico.mapper.UsuarioMapper;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {

        List<Usuario> usuarios = usuarioService.findAll();

        List<UsuarioResponseDto> response = usuarioMapper.toDto(usuarios);

        return ResponseEntity.ok(response);
    }

    //Listar apartir de um id
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@PathVariable UUID id) {

        Usuario usuario = usuarioService.findById(id);

        UsuarioResponseDto response = usuarioMapper.toDto(usuario);

        return ResponseEntity.ok(response);
    }

    //Atualizar usuario usando id
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        usuarioService.delete(id);

        return ResponseEntity.noContent().build();
    }


}