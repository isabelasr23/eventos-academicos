package br.com.eventosacademicos.eventos_academico.repository;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
