package br.com.eventosacademicos.eventos_academico.exception;

import java.util.UUID;

public class CategoriaNotFoundException extends RuntimeException {
    public CategoriaNotFoundException(UUID id) {
        super("Categoria não encontrada: " + id);
    }
}
