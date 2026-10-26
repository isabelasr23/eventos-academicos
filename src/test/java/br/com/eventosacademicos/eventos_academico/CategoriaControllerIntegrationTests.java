package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.service.EventoService;
import java.time.LocalDateTime;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import br.com.eventosacademicos.eventos_academico.service.impl.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaControllerIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EventoService eventoService;

    private String authorization;

    @BeforeEach
    void autenticar() {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        authorization = "Bearer " + jwtService.generateToken(usuario);
    }

    private String criarCategoria(String nome) throws Exception {
        String response = mvc.perform(post("/categorias")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome))
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    @Test
    void deveCadastrarListarConsultarAtualizarEExcluir() throws Exception {
        String id = criarCategoria("Palestra");

        mvc.perform(get("/categorias").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/categorias/" + id).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Palestra"));
        mvc.perform(put("/categorias/" + id).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Curso\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Curso"));
        mvc.perform(delete("/categorias/" + id).header("Authorization", authorization))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get("/categorias/" + id).header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409ParaNomeDuplicadoNoCadastroENaAtualizacao() throws Exception {
        criarCategoria("Palestra");
        String cursoId = criarCategoria("Curso");

        mvc.perform(post("/categorias").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Palestra\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Nome de categoria já cadastrado: Palestra"));
        mvc.perform(put("/categorias/" + cursoId).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Palestra\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/categorias/" + cursoId).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Curso"));
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        String path = "/categorias/" + UUID.randomUUID();

        mvc.perform(get(path).header("Authorization", authorization))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value(path));
        mvc.perform(put(path).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Curso\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete(path).header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveValidarNomeNoCadastroENaAtualizacao() throws Exception {
        String id = criarCategoria("Palestra");

        for (String body : new String[]{"{}", "{\"nome\":\"   \"}", "{\"nome\":\"" + "a".repeat(256) + "\"}"}) {
            mvc.perform(post("/categorias").header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/categorias/" + id).header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deveExigirTokenEmTodasAsOperacoes() throws Exception {
        String path = "/categorias/" + UUID.randomUUID();
        mvc.perform(get("/categorias")).andExpect(status().isUnauthorized());
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Curso\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Curso\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveDocumentarOperacoesRespostasEAutenticacao() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/categorias'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/categorias'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/categorias'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/categorias'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/categorias/{id}'].put.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/categorias/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/categorias/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/categorias/{id}'].delete.responses['409']").exists());
    }
    @Test
    void deveRetornar409AoExcluirCategoriaComEvento() throws Exception {
        String id = criarCategoria("Palestra");
        LocalDateTime inicio = LocalDateTime.of(2030, 11, 10, 9, 0);
        eventoService.create(new Evento(
                "Semana acadêmica", "Encontro dos estudantes", inicio, inicio.plusHours(3),
                50, new Categoria(UUID.fromString(id), null)
        ));

        mvc.perform(delete("/categorias/" + id).header("Authorization", authorization))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Categoria possui eventos vinculados: " + id));
        mvc.perform(get("/categorias/" + id).header("Authorization", authorization))
                .andExpect(status().isOk());
    }
}
