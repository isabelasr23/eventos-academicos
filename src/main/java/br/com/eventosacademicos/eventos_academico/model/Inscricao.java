package br.com.eventosacademicos.eventos_academico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_inscricao_usuario_evento",
        columnNames = {"usuario_id", "evento_id"}
))
public class Inscricao {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private LocalDateTime dataInscricao;

    //CONSTRUTORES

    public Inscricao() {
    }

    public Inscricao(Usuario usuario, Evento evento, LocalDateTime dataInscricao) {
        this.usuario = usuario;
        this.evento = evento;
        this.dataInscricao = dataInscricao;
    }

    public Inscricao(UUID id, Usuario usuario, Evento evento, LocalDateTime dataInscricao) {
        this(usuario, evento, dataInscricao);
        this.id = id;
    }

    //GETTERS AND SETTERS

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
    }
}
