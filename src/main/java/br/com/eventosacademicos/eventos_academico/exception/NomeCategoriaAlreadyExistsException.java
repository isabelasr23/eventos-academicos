package br.com.eventosacademicos.eventos_academico.exception;

public class NomeCategoriaAlreadyExistsException extends RuntimeException {
    public NomeCategoriaAlreadyExistsException(String nome) {
        super("Nome de categoria já cadastrado: " + nome);
    }
}
