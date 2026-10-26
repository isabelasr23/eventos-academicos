package br.com.eventosacademicos.eventos_academico.mapper;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateEventoRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.UpdateEventoRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.EventoResponseDto;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventoMapper {

    //Create
    public Evento toEntity(CreateEventoRequestDto dto){
        Categoria categoria = new Categoria();
        categoria.setId(dto.categoriaId());

        return new Evento(
                dto.titulo(),
                dto.descricao(),
                dto.inicio(),
                dto.fim(),
                dto.capacidade(),
                categoria
        );
    }

    //Update
    public Evento toEntity(UpdateEventoRequestDto dto){
        Categoria categoria = new Categoria();
        categoria.setId(dto.categoriaId());

        return new Evento(
                dto.titulo(),
                dto.descricao(),
                dto.inicio(),
                dto.fim(),
                dto.capacidade(),
                categoria
        );
    }

    public EventoResponseDto toDto(Evento evento){
        return new EventoResponseDto(
                evento.getId(),
                evento.getTitulo(),
                evento.getDescricao(),
                evento.getInicio(),
                evento.getFim(),
                evento.getCapacidade(),
                evento.getCategoria().getId()
        );
    }

    public List<EventoResponseDto> toDto(List<Evento> eventos) {
        return eventos.stream()
                .map(this::toDto)
                .toList();
    }
}
