package br.com.eventosacademicos.eventos_academico.exception;

public class InscricaoNaoPermitidaException extends RuntimeException {
    public InscricaoNaoPermitidaException() {
        super("A inscrição pertence a outro usuário");
    }
}
