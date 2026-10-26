package br.com.eventosacademicos.eventos_academico.mapper;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateCategoriaRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateCategoriaRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.CategoriaResponseDto;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriaMapper {

    //Create
    public Categoria toEntity(CreateCategoriaRequestDto dto){
        return new Categoria(
                dto.nome()
        );
    }

    //Update
    public Categoria toEntity(UpdateCategoriaRequestDto dto){
        return new Categoria(
                dto.nome()
        );
    }

    public CategoriaResponseDto toDto(Categoria categoria){
        return new CategoriaResponseDto(
                categoria.getId(),
                categoria.getNome()
        );
    }

    public List<CategoriaResponseDto> toDto(List<Categoria> categorias) {
        return categorias.stream()
                .map(this::toDto)
                .toList();
    }
}
