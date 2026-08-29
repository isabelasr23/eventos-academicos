package br.com.eventosacademicos.eventos_academico.mapper;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.UsuarioResponseDto;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    //Create
    public Usuario toEntity(CreateUsuarioRequestDto dto){
        return new Usuario(
                dto.nome(),
                dto.email(),
                dto.senha()
        );
    }

    //Update
    public Usuario toEntity(UpdateUsuarioRequestDto dto){
        return new Usuario(
                dto.nome(),
                dto.email(),
                dto.senha()
        );
    }



    public UsuarioResponseDto toDto(Usuario usuario){
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }

    public List<UsuarioResponseDto> toDto(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(this::toDto)
                .toList();
    }
}
