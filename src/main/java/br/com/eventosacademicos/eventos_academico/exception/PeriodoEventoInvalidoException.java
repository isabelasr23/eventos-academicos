package br.com.eventosacademicos.eventos_academico.exception;

public class PeriodoEventoInvalidoException extends RuntimeException {
    public PeriodoEventoInvalidoException() {
        super("O fim do evento deve ser posterior ao início");
    }
}
