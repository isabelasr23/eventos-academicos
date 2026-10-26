package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class EventoNotFoundException extends RuntimeException {
    public EventoNotFoundException(UUID id) {
        super("Evento não encontrado: " + id);
    }
}
