package br.com.eventosacademicos.eventos_academico.repository;

import br.com.eventosacademicos.eventos_academico.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, UUID> {
    List<Inscricao> findByUsuarioId(UUID usuarioId);

    boolean existsByUsuarioIdAndEventoId(UUID usuarioId, UUID eventoId);

    long countByEventoId(UUID eventoId);

    boolean existsByUsuarioId(UUID usuarioId);

    boolean existsByEventoId(UUID eventoId);
}
