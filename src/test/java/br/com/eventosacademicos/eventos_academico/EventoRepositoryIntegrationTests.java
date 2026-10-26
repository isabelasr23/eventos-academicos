package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.repository.CategoriaRepository;
import br.com.eventosacademicos.eventos_academico.repository.EventoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventoRepositoryIntegrationTests {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirRelacionamentoEIdentificarCategoriaComEventos() {
        Categoria palestra = categoriaRepository.saveAndFlush(new Categoria("Palestra"));
        Categoria curso = categoriaRepository.saveAndFlush(new Categoria("Curso"));
        LocalDateTime inicio = LocalDateTime.of(2030, 11, 10, 9, 0);
        LocalDateTime fim = inicio.plusHours(3);

        Evento criado = eventoRepository.saveAndFlush(new Evento(
                "Semana acadêmica", "Encontro dos estudantes", inicio, fim, 50, palestra
        ));
        entityManager.clear();

        Evento salvo = eventoRepository.findById(criado.getId()).orElseThrow();

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getInicio()).isEqualTo(inicio);
        assertThat(salvo.getFim()).isEqualTo(fim);
        assertThat(salvo.getCategoria().getId()).isEqualTo(palestra.getId());
        assertThat(salvo.getCategoria().getNome()).isEqualTo("Palestra");
        assertThat(eventoRepository.existsByCategoriaId(palestra.getId())).isTrue();
        assertThat(eventoRepository.existsByCategoriaId(curso.getId())).isFalse();
    }
}
