package br.com.eventosacademicos.eventos_academico.repository;

import br.com.eventosacademicos.eventos_academico.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {
    boolean existsByCategoriaId(UUID categoriaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select evento from Evento evento where evento.id = :id")
    Optional<Evento> findByIdComBloqueio(@Param("id") UUID id);
}
