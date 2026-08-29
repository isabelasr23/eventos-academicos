package br.com.eventosacademicos.eventos_academico.service;

import br.com.eventosacademicos.eventos_academico.model.Usuario;

import java.util.List;
import java.util.UUID;

public interface UsuarioService {
    Usuario create(Usuario usuario);

    List<Usuario> findAll();

    Usuario findById(UUID id);

    Usuario update(UUID id ,Usuario usuario);

    void delete(UUID id);
}
