package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.exception.*;
import br.com.eventosacademicos.eventos_academico.model.*;
import br.com.eventosacademicos.eventos_academico.repository.InscricaoRepository;
import br.com.eventosacademicos.eventos_academico.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class InscricaoServiceIntegrationTests {

    @Autowired private InscricaoService inscricaoService;
    @Autowired private InscricaoRepository inscricaoRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private EventoService eventoService;

    private Usuario maria;
    private Usuario ana;
    private Evento evento;

    @BeforeEach
    void preparar() {
        maria = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        ana = usuarioService.create(new Usuario("Ana", "ana@example.com", "senha123"));
        Categoria categoria = categoriaService.create(new Categoria("Palestra"));
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withNano(0);
        evento = eventoService.create(new Evento("Semana acadêmica", "Encontro", inicio, inicio.plusHours(3), 2, categoria));
    }

    private Inscricao entrada(UUID eventoId) {
        Evento referencia = new Evento();
        referencia.setId(eventoId);
        Inscricao inscricao = new Inscricao();
        inscricao.setEvento(referencia);
        return inscricao;
    }

    @Test
    void deveInscreverListarConsultarECancelarUsandoUsuarioEDataDoSistema() {
        LocalDateTime antes = LocalDateTime.now();
        Inscricao request = entrada(evento.getId());
        request.setUsuario(ana);
        request.setDataInscricao(antes.minusDays(10));
        Inscricao criada = inscricaoService.create(request, maria.getId());

        assertThat(criada.getUsuario().getId()).isEqualTo(maria.getId());
        assertThat(criada.getDataInscricao()).isBetween(antes, LocalDateTime.now());
        assertThat(inscricaoService.findAll(maria.getId())).extracting(Inscricao::getId).containsExactly(criada.getId());
        assertThat(inscricaoService.findAll(ana.getId())).isEmpty();
        assertThat(inscricaoService.findById(criada.getId(), maria.getId()).getEvento().getId()).isEqualTo(evento.getId());

        inscricaoService.delete(criada.getId(), maria.getId());
        inscricaoRepository.flush();
        assertThat(inscricaoService.findAll(maria.getId())).isEmpty();
    }

    @Test
    void deveRecusarDuplicidade() {
        inscricaoService.create(entrada(evento.getId()), maria.getId());
        assertThatThrownBy(() -> inscricaoService.create(entrada(evento.getId()), maria.getId()))
                .isInstanceOf(InscricaoInvalidaException.class).hasMessage("Usuário já inscrito neste evento");
        assertThat(inscricaoRepository.countByEventoId(evento.getId())).isEqualTo(1);
    }

    @Test
    void deveRecusarEventoSemVagasELiberarVagaAposCancelamento() {
        evento.setCapacidade(1);
        eventoService.update(evento.getId(), evento);
        Inscricao criada = inscricaoService.create(entrada(evento.getId()), maria.getId());
        assertThatThrownBy(() -> inscricaoService.create(entrada(evento.getId()), ana.getId()))
                .isInstanceOf(InscricaoInvalidaException.class).hasMessage("Evento sem vagas disponíveis");

        inscricaoService.delete(criada.getId(), maria.getId());
        assertThat(inscricaoService.create(entrada(evento.getId()), ana.getId()).getUsuario().getId()).isEqualTo(ana.getId());
    }

    @Test
    void deveRecusarEventoJaIniciado() {
        evento.setInicio(LocalDateTime.now().minusHours(1));
        evento.setFim(LocalDateTime.now().plusHours(1));
        eventoService.update(evento.getId(), evento);
        assertThatThrownBy(() -> inscricaoService.create(entrada(evento.getId()), maria.getId()))
                .isInstanceOf(InscricaoInvalidaException.class).hasMessage("As inscrições encerraram para este evento");
    }

    @Test
    void deveRecusarConsultaECancelamentoDeOutroUsuario() {
        Inscricao criada = inscricaoService.create(entrada(evento.getId()), maria.getId());
        assertThatThrownBy(() -> inscricaoService.findById(criada.getId(), ana.getId()))
                .isInstanceOf(InscricaoNaoPermitidaException.class);
        assertThatThrownBy(() -> inscricaoService.delete(criada.getId(), ana.getId()))
                .isInstanceOf(InscricaoNaoPermitidaException.class);
    }

    @Test
    void deveTratarUsuarioEventoEInscricaoInexistentes() {
        assertThatThrownBy(() -> inscricaoService.create(entrada(evento.getId()), UUID.randomUUID()))
                .isInstanceOf(UsuarioNotFoundException.class);
        assertThatThrownBy(() -> inscricaoService.create(entrada(UUID.randomUUID()), maria.getId()))
                .isInstanceOf(EventoNotFoundException.class);
        assertThatThrownBy(() -> inscricaoService.findById(UUID.randomUUID(), maria.getId()))
                .isInstanceOf(InscricaoNotFoundException.class);
        assertThatThrownBy(() -> inscricaoService.delete(UUID.randomUUID(), maria.getId()))
                .isInstanceOf(InscricaoNotFoundException.class);
    }

    @Test
    void deveBloquearExclusoesComVinculosELiberarAposCancelamento() {
        Inscricao criada = inscricaoService.create(entrada(evento.getId()), maria.getId());
        assertThatThrownBy(() -> eventoService.delete(evento.getId())).isInstanceOf(EventoEmUsoException.class);
        assertThatThrownBy(() -> usuarioService.delete(maria.getId())).isInstanceOf(UsuarioEmUsoException.class);

        inscricaoService.delete(criada.getId(), maria.getId());
        eventoService.delete(evento.getId());
        usuarioService.delete(maria.getId());
        inscricaoRepository.flush();
        assertThat(inscricaoRepository.findById(criada.getId())).isEmpty();
    }

    @Test
    void deveRecusarCapacidadeMenorQueTotalDeInscritos() {
        inscricaoService.create(entrada(evento.getId()), maria.getId());
        inscricaoService.create(entrada(evento.getId()), ana.getId());
        Evento alteracao = new Evento("Título novo", "Descrição", evento.getInicio(), evento.getFim(), 1, evento.getCategoria());
        assertThatThrownBy(() -> eventoService.update(evento.getId(), alteracao))
                .isInstanceOf(CapacidadeEventoInvalidaException.class);
        assertThat(eventoService.findById(evento.getId()).getCapacidade()).isEqualTo(2);
    }
}
