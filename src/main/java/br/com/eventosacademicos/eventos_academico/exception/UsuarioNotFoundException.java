package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(UUID id) {
        super("Usuário não encontrado: " + id);
    }
}
