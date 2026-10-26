package br.com.eventosacademicos.eventos_academico.service;

import br.com.eventosacademicos.eventos_academico.model.Inscricao;

import java.util.List;
import java.util.UUID;

public interface InscricaoService {
    Inscricao create(Inscricao inscricao, UUID usuarioId);

    List<Inscricao> findAll(UUID usuarioId);

    Inscricao findById(UUID id, UUID usuarioId);

    void delete(UUID id, UUID usuarioId);
}
