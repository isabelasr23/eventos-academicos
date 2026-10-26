package br.com.eventosacademicos.eventos_academico.service;

import br.com.eventosacademicos.eventos_academico.model.Categoria;

import java.util.List;
import java.util.UUID;

public interface CategoriaService {
    Categoria create(Categoria categoria);

    List<Categoria> findAll();

    Categoria findById(UUID id);

    Categoria update(UUID id, Categoria categoria);

    void delete(UUID id);
}
