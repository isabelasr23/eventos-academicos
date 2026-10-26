package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class InscricaoNotFoundException extends RuntimeException {
    public InscricaoNotFoundException(UUID id) {
        super("Inscrição não encontrada: " + id);
    }
}
