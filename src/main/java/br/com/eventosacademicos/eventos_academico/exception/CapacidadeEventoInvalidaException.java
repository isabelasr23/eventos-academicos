package br.com.eventosacademicos.eventos_academico.exception;

public class CapacidadeEventoInvalidaException extends RuntimeException {
    public CapacidadeEventoInvalidaException() {
        super("A capacidade não pode ser inferior ao total de inscritos");
    }
}
