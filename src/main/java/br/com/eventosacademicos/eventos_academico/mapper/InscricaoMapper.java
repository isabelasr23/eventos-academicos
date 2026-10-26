package br.com.eventosacademicos.eventos_academico.mapper;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateInscricaoRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.response.InscricaoResponseDto;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.model.Inscricao;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InscricaoMapper {

    //Create
    public Inscricao toEntity(CreateInscricaoRequestDto dto){
        Evento evento = new Evento();
        evento.setId(dto.eventoId());

        Inscricao inscricao = new Inscricao();
        inscricao.setEvento(evento);

        return inscricao;
    }

    public InscricaoResponseDto toDto(Inscricao inscricao){
        return new InscricaoResponseDto(
                inscricao.getId(),
                inscricao.getUsuario().getId(),
                inscricao.getEvento().getId(),
                inscricao.getDataInscricao()
        );
    }

    public List<InscricaoResponseDto> toDto(List<Inscricao> inscricoes) {
        return inscricoes.stream()
                .map(this::toDto)
                .toList();
    }
}
