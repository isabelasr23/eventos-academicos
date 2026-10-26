package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class UsuarioEmUsoException extends RuntimeException {
    public UsuarioEmUsoException(UUID id) {
        super("Usuário possui inscrições vinculadas: " + id);
    }
}
