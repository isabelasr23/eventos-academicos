package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.exception.InscricaoInvalidaException;
import br.com.eventosacademicos.eventos_academico.model.*;
import br.com.eventosacademicos.eventos_academico.repository.InscricaoRepository;
import br.com.eventosacademicos.eventos_academico.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InscricaoConcorrenciaIntegrationTests {

    @Autowired private InscricaoService inscricaoService;
    @Autowired private InscricaoRepository inscricaoRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private EventoService eventoService;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    void devePermitirApenasUmaInscricaoNaDisputaPelaUltimaVaga() throws Exception {
        UUID[] ids = transactionTemplate.execute(status -> {
            String sufixo = UUID.randomUUID().toString();
            Usuario maria = usuarioService.create(new Usuario("Maria", "maria-" + sufixo + "@example.com", "senha123"));
            Usuario ana = usuarioService.create(new Usuario("Ana", "ana-" + sufixo + "@example.com", "senha123"));
            Categoria categoria = categoriaService.create(new Categoria("Palestra " + sufixo));
            LocalDateTime inicio = LocalDateTime.now().plusDays(2);
            Evento evento = eventoService.create(new Evento("Semana acadêmica", "Encontro", inicio, inicio.plusHours(3), 1, categoria));
            return new UUID[]{maria.getId(), ana.getId(), categoria.getId(), evento.getId()};
        });

        try {
            CountDownLatch inicio = new CountDownLatch(1);
            try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
                Future<Boolean> primeira = executor.submit(() -> inscrever(ids[0], ids[3], inicio));
                Future<Boolean> segunda = executor.submit(() -> inscrever(ids[1], ids[3], inicio));
                inicio.countDown();
                int aceitas = (primeira.get(10, TimeUnit.SECONDS) ? 1 : 0) + (segunda.get(10, TimeUnit.SECONDS) ? 1 : 0);
                assertThat(aceitas).isEqualTo(1);
                assertThat(inscricaoRepository.countByEventoId(ids[3])).isEqualTo(1);
            }
        } finally {
            transactionTemplate.executeWithoutResult(status -> {
                inscricaoRepository.deleteAll(inscricaoRepository.findByUsuarioId(ids[0]));
                inscricaoRepository.deleteAll(inscricaoRepository.findByUsuarioId(ids[1]));
                inscricaoRepository.flush();
                eventoService.delete(ids[3]);
                usuarioService.delete(ids[0]);
                usuarioService.delete(ids[1]);
                categoriaService.delete(ids[2]);
            });
        }
    }

    private boolean inscrever(UUID usuarioId, UUID eventoId, CountDownLatch inicio) throws InterruptedException {
        inicio.await();
        Evento evento = new Evento();
        evento.setId(eventoId);
        Inscricao inscricao = new Inscricao();
        inscricao.setEvento(evento);
        try {
            inscricaoService.create(inscricao, usuarioId);
            return true;
        } catch (InscricaoInvalidaException ex) {
            assertThat(ex.getMessage()).isEqualTo("Evento sem vagas disponíveis");
            return false;
        }
    }
}
