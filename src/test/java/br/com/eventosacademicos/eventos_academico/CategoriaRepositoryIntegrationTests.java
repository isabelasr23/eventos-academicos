package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoriaRepositoryIntegrationTests {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveDetectarNomeDuplicadoSemConsiderarAPropriaCategoria() {
        Categoria palestra = categoriaRepository.saveAndFlush(new Categoria("Palestra"));
        Categoria curso = categoriaRepository.saveAndFlush(new Categoria("Curso"));

        assertThat(palestra.getId()).isNotNull();
        assertThat(categoriaRepository.existsByNome("Palestra")).isTrue();
        assertThat(categoriaRepository.existsByNome("Congresso")).isFalse();
        assertThat(categoriaRepository.existsByNomeAndIdNot("Palestra", palestra.getId())).isFalse();
        assertThat(categoriaRepository.existsByNomeAndIdNot("Palestra", curso.getId())).isTrue();
        assertThat(categoriaRepository.findById(palestra.getId())).isPresent();
    }
}
