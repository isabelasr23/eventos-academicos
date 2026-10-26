package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.exception.CategoriaNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.NomeCategoriaAlreadyExistsException;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.repository.CategoriaRepository;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoriaServiceIntegrationTests {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveCriarListarBuscarAtualizarEExcluirCategoria() {
        Categoria criada = categoriaService.create(new Categoria("Palestra"));
        UUID id = criada.getId();

        assertThat(id).isNotNull();
        assertThat(categoriaService.findAll()).extracting(Categoria::getId).contains(id);
        assertThat(categoriaService.findById(id).getNome()).isEqualTo("Palestra");

        Categoria atualizada = categoriaService.update(id, new Categoria("Curso"));
        categoriaRepository.flush();

        assertThat(atualizada.getId()).isEqualTo(id);
        assertThat(categoriaService.findById(id).getNome()).isEqualTo("Curso");

        categoriaService.delete(id);
        categoriaRepository.flush();

        assertThat(categoriaRepository.findById(id)).isEmpty();
    }

    @Test
    void deveRecusarNomeDuplicadoNoCadastro() {
        categoriaService.create(new Categoria("Palestra"));

        assertThatThrownBy(() -> categoriaService.create(new Categoria("Palestra")))
                .isInstanceOf(NomeCategoriaAlreadyExistsException.class);
        assertThat(categoriaRepository.count()).isEqualTo(1);
    }

    @Test
    void deveRecusarNomeDeOutraCategoriaNaAtualizacao() {
        categoriaService.create(new Categoria("Palestra"));
        Categoria curso = categoriaService.create(new Categoria("Curso"));

        assertThatThrownBy(() -> categoriaService.update(curso.getId(), new Categoria("Palestra")))
                .isInstanceOf(NomeCategoriaAlreadyExistsException.class);
        assertThat(categoriaService.findById(curso.getId()).getNome()).isEqualTo("Curso");
    }

    @Test
    void devePermitirManterOProprioNomeNaAtualizacao() {
        Categoria criada = categoriaService.create(new Categoria("Palestra"));

        Categoria atualizada = categoriaService.update(criada.getId(), new Categoria("Palestra"));

        assertThat(atualizada.getId()).isEqualTo(criada.getId());
        assertThat(atualizada.getNome()).isEqualTo("Palestra");
    }

    @Test
    void deveRecusarBuscaAtualizacaoEExclusaoDeCategoriaInexistente() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> categoriaService.findById(id))
                .isInstanceOf(CategoriaNotFoundException.class);
        assertThatThrownBy(() -> categoriaService.update(id, new Categoria("Curso")))
                .isInstanceOf(CategoriaNotFoundException.class);
        assertThatThrownBy(() -> categoriaService.delete(id))
                .isInstanceOf(CategoriaNotFoundException.class);
    }
}
