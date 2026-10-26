package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class CategoriaEmUsoException extends RuntimeException {
    public CategoriaEmUsoException(UUID id) {
        super("Categoria possui eventos vinculados: " + id);
    }
}
