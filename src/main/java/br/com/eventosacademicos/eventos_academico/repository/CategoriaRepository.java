package br.com.eventosacademicos.eventos_academico.repository;

import br.com.eventosacademicos.eventos_academico.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, UUID id);
}
