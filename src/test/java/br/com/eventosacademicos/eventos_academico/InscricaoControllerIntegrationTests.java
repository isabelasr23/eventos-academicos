package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
import br.com.eventosacademicos.eventos_academico.service.EventoService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InscricaoControllerIntegrationTests {

    @Autowired private MockMvc mvc;
    @Autowired private CategoriaService categoriaService;
    @Autowired private EventoService eventoService;
    @Autowired private UsuarioRepository usuarioRepository;

    private String mariaToken;
    private String anaToken;
    private UUID mariaId;
    private UUID anaId;
    private Evento evento;

    @BeforeEach
    void preparar() throws Exception {
        mariaToken = cadastrar("Maria", "maria@example.com");
        anaToken = cadastrar("Ana", "ana@example.com");
        mariaId = usuarioRepository.findByEmail("maria@example.com").orElseThrow().getId();
        anaId = usuarioRepository.findByEmail("ana@example.com").orElseThrow().getId();
        Categoria categoria = categoriaService.create(new Categoria("Palestra"));
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withNano(0);
        evento = eventoService.create(new Evento("Semana acadêmica", "Encontro", inicio, inicio.plusHours(3), 1, categoria));
    }

    private String cadastrar(String nome, String email) throws Exception {
        String response = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(response, "$.token");
    }

    private String dados() {
        return "{\"eventoId\":\"" + evento.getId() + "\"}";
    }

    private String inscrever(String token) throws Exception {
        String response = mvc.perform(post("/inscricoes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(dados()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value(evento.getId().toString()))
                .andExpect(jsonPath("$.dataInscricao").isString())
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.evento").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    @Test
    void deveInscreverListarConsultarECancelar() throws Exception {
        String id = inscrever(mariaToken);
        mvc.perform(get("/inscricoes").header("Authorization", mariaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/inscricoes/" + id).header("Authorization", mariaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(mariaId.toString()));
        mvc.perform(delete("/inscricoes/" + id).header("Authorization", mariaToken))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/inscricoes/" + id).header("Authorization", mariaToken))
                .andExpect(status().isNotFound());
        mvc.perform(get("/inscricoes").header("Authorization", mariaToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveIgnorarUsuarioEDataEnviadosNoCorpo() throws Exception {
        String body = "{\"eventoId\":\"" + evento.getId() + "\",\"usuarioId\":\"" + anaId
                + "\",\"dataInscricao\":\"2000-01-01T00:00:00\"}";
        String response = mvc.perform(post("/inscricoes").header("Authorization", mariaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(mariaId.toString()))
                .andReturn().getResponse().getContentAsString();
        String data = JsonPath.read(response, "$.dataInscricao");
        org.assertj.core.api.Assertions.assertThat(LocalDateTime.parse(data)).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void deveRestringirConsultaECancelamentoAoProprietario() throws Exception {
        String id = inscrever(mariaToken);
        mvc.perform(get("/inscricoes").header("Authorization", anaToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/inscricoes/" + id).header("Authorization", anaToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("A inscrição pertence a outro usuário"));
        mvc.perform(delete("/inscricoes/" + id).header("Authorization", anaToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/inscricoes/" + id).header("Authorization", mariaToken))
                .andExpect(status().isOk());
    }

    @Test
    void deveRecusarDuplicidadeEFaltaDeVagasELiberarAposCancelar() throws Exception {
        String id = inscrever(mariaToken);
        mvc.perform(post("/inscricoes").header("Authorization", mariaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(dados()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Usuário já inscrito neste evento"));
        mvc.perform(post("/inscricoes").header("Authorization", anaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(dados()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Evento sem vagas disponíveis"));
        mvc.perform(delete("/inscricoes/" + id).header("Authorization", mariaToken))
                .andExpect(status().isNoContent());
        inscrever(anaToken);
    }

    @Test
    void deveTratarEventoEInscricaoInexistentes() throws Exception {
        String path = "/inscricoes/" + UUID.randomUUID();
        mvc.perform(get(path).header("Authorization", mariaToken)).andExpect(status().isNotFound());
        mvc.perform(delete(path).header("Authorization", mariaToken)).andExpect(status().isNotFound());
        mvc.perform(post("/inscricoes").header("Authorization", mariaToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExigirEventoNoDto() throws Exception {
        mvc.perform(post("/inscricoes").header("Authorization", mariaToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirTokenEmTodasAsOperacoes() throws Exception {
        String path = "/inscricoes/" + UUID.randomUUID();
        mvc.perform(get("/inscricoes")).andExpect(status().isUnauthorized());
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post("/inscricoes").contentType(MediaType.APPLICATION_JSON).content(dados()))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarEventoJaIniciado() throws Exception {
        evento.setInicio(LocalDateTime.now().minusHours(1));
        evento.setFim(LocalDateTime.now().plusHours(1));
        eventoService.update(evento.getId(), evento);
        mvc.perform(post("/inscricoes").header("Authorization", mariaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(dados()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("As inscrições encerraram para este evento"));
    }

    @Test
    void deveRetornar409ParaExclusoesComVinculosEReducaoDeCapacidade() throws Exception {
        evento.setCapacidade(2);
        eventoService.update(evento.getId(), evento);
        inscrever(mariaToken);
        inscrever(anaToken);
        mvc.perform(delete("/eventos/" + evento.getId()).header("Authorization", mariaToken))
                .andExpect(status().isConflict());
        mvc.perform(delete("/usuarios/" + mariaId).header("Authorization", mariaToken))
                .andExpect(status().isConflict());
        String body = """
                {"titulo":"Semana acadêmica","descricao":"Encontro","inicio":"%s","fim":"%s",
                "capacidade":1,"categoriaId":"%s"}
                """.formatted(evento.getInicio(), evento.getFim(), evento.getCategoria().getId());
        mvc.perform(put("/eventos/" + evento.getId()).header("Authorization", mariaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A capacidade não pode ser inferior ao total de inscritos"));
    }

    @Test
    void deveDocumentarRotasRespostasEUsuarioDoToken() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/inscricoes'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/inscricoes'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/inscricoes'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/inscricoes'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/inscricoes'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/inscricoes'].get.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/inscricoes/{id}'].get.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/inscricoes/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/inscricoes/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.components.schemas.CreateInscricaoRequestDto.properties.eventoId").exists())
                .andExpect(jsonPath("$.components.schemas.CreateInscricaoRequestDto.properties.usuarioId").doesNotExist());
    }
}
