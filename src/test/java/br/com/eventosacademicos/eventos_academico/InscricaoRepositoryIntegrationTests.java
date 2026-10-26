package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.model.Inscricao;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.repository.CategoriaRepository;
import br.com.eventosacademicos.eventos_academico.repository.EventoRepository;
import br.com.eventosacademicos.eventos_academico.repository.InscricaoRepository;
import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class InscricaoRepositoryIntegrationTests {

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirVinculosEConsultarInscricoesPorUsuarioEEvento() {
        Usuario maria = usuarioRepository.saveAndFlush(new Usuario("Maria", "maria@example.com", "hash-de-teste"));
        Usuario ana = usuarioRepository.saveAndFlush(new Usuario("Ana", "ana@example.com", "hash-de-teste"));
        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Palestra"));
        LocalDateTime inicio = LocalDateTime.of(2030, 11, 10, 9, 0);
        LocalDateTime dataInscricao = inicio.minusDays(1);
        Evento primeiro = eventoRepository.saveAndFlush(new Evento(
                "Semana acadêmica", "Encontro dos estudantes", inicio, inicio.plusHours(3), 50, categoria
        ));
        Evento segundo = eventoRepository.saveAndFlush(new Evento(
                "Palestra de Java", "Introdução ao Java", inicio, inicio.plusHours(2), 30, categoria
        ));
        Inscricao criada = inscricaoRepository.saveAndFlush(new Inscricao(maria, primeiro, dataInscricao));
        Inscricao outraDaMaria = inscricaoRepository.saveAndFlush(new Inscricao(maria, segundo, dataInscricao));
        inscricaoRepository.saveAndFlush(new Inscricao(ana, primeiro, dataInscricao));
        entityManager.clear();

        Inscricao salva = inscricaoRepository.findById(criada.getId()).orElseThrow();
        assertThat(salva.getUsuario().getId()).isEqualTo(maria.getId());
        assertThat(salva.getEvento().getId()).isEqualTo(primeiro.getId());
        assertThat(salva.getDataInscricao()).isEqualTo(dataInscricao);
        assertThat(inscricaoRepository.findByUsuarioId(maria.getId()))
                .extracting(Inscricao::getId).containsExactlyInAnyOrder(criada.getId(), outraDaMaria.getId());
        assertThat(inscricaoRepository.findByUsuarioId(UUID.randomUUID())).isEmpty();
        assertThat(inscricaoRepository.existsByUsuarioIdAndEventoId(maria.getId(), primeiro.getId())).isTrue();
        assertThat(inscricaoRepository.existsByUsuarioIdAndEventoId(ana.getId(), segundo.getId())).isFalse();
        assertThat(inscricaoRepository.countByEventoId(primeiro.getId())).isEqualTo(2);
        assertThat(inscricaoRepository.countByEventoId(segundo.getId())).isEqualTo(1);
        assertThat(inscricaoRepository.countByEventoId(UUID.randomUUID())).isZero();
        assertThat(inscricaoRepository.existsByUsuarioId(maria.getId())).isTrue();
        assertThat(inscricaoRepository.existsByUsuarioId(UUID.randomUUID())).isFalse();
        assertThat(inscricaoRepository.existsByEventoId(primeiro.getId())).isTrue();
        assertThat(inscricaoRepository.existsByEventoId(UUID.randomUUID())).isFalse();
    }
}
