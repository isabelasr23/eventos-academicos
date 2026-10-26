package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class EventoEmUsoException extends RuntimeException {
    public EventoEmUsoException(UUID id) {
        super("Evento possui inscrições vinculadas: " + id);
    }
}
