package br.com.eventosacademicos.eventos_academico.service;

import br.com.eventosacademicos.eventos_academico.model.Evento;

import java.util.List;
import java.util.UUID;

public interface EventoService {
    Evento create(Evento evento);

    List<Evento> findAll();

    Evento findById(UUID id);

    Evento update(UUID id, Evento evento);

    void delete(UUID id);
}
