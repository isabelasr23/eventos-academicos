package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.exception.CategoriaEmUsoException;
import br.com.eventosacademicos.eventos_academico.exception.CategoriaNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.EventoNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.PeriodoEventoInvalidoException;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.repository.EventoRepository;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
import br.com.eventosacademicos.eventos_academico.service.EventoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EventoServiceIntegrationTests {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EntityManager entityManager;

    private Evento novoEvento(UUID categoriaId) {
        LocalDateTime inicio = LocalDateTime.of(2030, 11, 10, 9, 0);
        return new Evento("Semana acadêmica", "Encontro dos estudantes", inicio, inicio.plusHours(3),
                50, new Categoria(categoriaId, null));
    }

    @Test
    void deveCriarListarConsultarAtualizarEExcluirEvento() {
        Categoria palestra = categoriaService.create(new Categoria("Palestra"));
        Categoria curso = categoriaService.create(new Categoria("Curso"));
        Evento criado = eventoService.create(novoEvento(palestra.getId()));
        UUID id = criado.getId();

        assertThat(eventoService.findAll()).extracting(Evento::getId).contains(id);
        assertThat(eventoService.findById(id).getCategoria().getNome()).isEqualTo("Palestra");

        Evento entrada = novoEvento(curso.getId());
        entrada.setTitulo("Curso de Java");
        entrada.setCapacidade(30);
        eventoService.update(id, entrada);
        eventoRepository.flush();
        entityManager.clear();

        Evento atualizado = eventoService.findById(id);
        assertThat(atualizado.getTitulo()).isEqualTo("Curso de Java");
        assertThat(atualizado.getCapacidade()).isEqualTo(30);
        assertThat(atualizado.getCategoria().getId()).isEqualTo(curso.getId());

        eventoService.delete(id);
        eventoRepository.flush();
        assertThat(eventoRepository.findById(id)).isEmpty();
    }

    @Test
    void deveRecusarFimIgualOuAnteriorAoInicioNoCadastro() {
        Categoria categoria = categoriaService.create(new Categoria("Palestra"));
        for (int horas : new int[]{0, -1}) {
            Evento entrada = novoEvento(categoria.getId());
            entrada.setFim(entrada.getInicio().plusHours(horas));
            assertThatThrownBy(() -> eventoService.create(entrada))
                    .isInstanceOf(PeriodoEventoInvalidoException.class);
        }
        assertThat(eventoRepository.count()).isZero();
    }

    @Test
    void deveRecusarPeriodoInvalidoNaAtualizacaoSemAlterarRegistro() {
        Categoria categoria = categoriaService.create(new Categoria("Palestra"));
        Evento criado = eventoService.create(novoEvento(categoria.getId()));
        Evento entrada = novoEvento(categoria.getId());
        entrada.setTitulo("Título que não deve ser salvo");
        entrada.setFim(entrada.getInicio());

        assertThatThrownBy(() -> eventoService.update(criado.getId(), entrada))
                .isInstanceOf(PeriodoEventoInvalidoException.class);
        assertThat(eventoService.findById(criado.getId()).getTitulo()).isEqualTo("Semana acadêmica");
    }

    @Test
    void deveRecusarCategoriaInexistenteNoCadastroENaAtualizacao() {
        Evento entrada = novoEvento(UUID.randomUUID());
        assertThatThrownBy(() -> eventoService.create(entrada)).isInstanceOf(CategoriaNotFoundException.class);

        Categoria categoria = categoriaService.create(new Categoria("Palestra"));
        Evento criado = eventoService.create(novoEvento(categoria.getId()));
        assertThatThrownBy(() -> eventoService.update(criado.getId(), entrada))
                .isInstanceOf(CategoriaNotFoundException.class);
        assertThat(eventoService.findById(criado.getId()).getCategoria().getId()).isEqualTo(categoria.getId());
    }

    @Test
    void deveRecusarBuscaAtualizacaoEExclusaoDeEventoInexistente() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> eventoService.findById(id)).isInstanceOf(EventoNotFoundException.class);
        assertThatThrownBy(() -> eventoService.update(id, novoEvento(UUID.randomUUID())))
                .isInstanceOf(EventoNotFoundException.class);
        assertThatThrownBy(() -> eventoService.delete(id)).isInstanceOf(EventoNotFoundException.class);
    }

    @Test
    void deveBloquearExclusaoDeCategoriaAteRemoverSeuEvento() {
        Categoria categoria = categoriaService.create(new Categoria("Palestra"));
        Evento criado = eventoService.create(novoEvento(categoria.getId()));

        assertThatThrownBy(() -> categoriaService.delete(categoria.getId()))
                .isInstanceOf(CategoriaEmUsoException.class);
        assertThat(categoriaService.findById(categoria.getId())).isNotNull();

        eventoService.delete(criado.getId());
        categoriaService.delete(categoria.getId());
        eventoRepository.flush();
        assertThatThrownBy(() -> categoriaService.findById(categoria.getId()))
                .isInstanceOf(CategoriaNotFoundException.class);
    }
}
