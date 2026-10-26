package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
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
class EventoControllerIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private JwtService jwtService;

    private String authorization;
    private UUID categoriaId;

    @BeforeEach
    void preparar() {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        authorization = "Bearer " + jwtService.generateToken(usuario);
        categoriaId = categoriaService.create(new Categoria("Palestra")).getId();
    }

    private String dados(UUID categoria) {
        return """
                {"titulo":"Semana acadêmica","descricao":"Encontro dos estudantes",
                "inicio":"2030-11-10T09:00:00","fim":"2030-11-10T12:00:00",
                "capacidade":50,"categoriaId":"%s"}
                """.formatted(categoria);
    }

    private String criarEvento() throws Exception {
        String response = mvc.perform(post("/eventos").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(dados(categoriaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Semana acadêmica"))
                .andExpect(jsonPath("$.categoriaId").value(categoriaId.toString()))
                .andExpect(jsonPath("$.categoria").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    @Test
    void deveCadastrarListarConsultarAtualizarEExcluir() throws Exception {
        String id = criarEvento();
        UUID outraCategoria = categoriaService.create(new Categoria("Curso")).getId();

        mvc.perform(get("/eventos").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/eventos/" + id).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.capacidade").value(50));
        mvc.perform(put("/eventos/" + id).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados(outraCategoria).replace("Semana acadêmica", "Curso de Java")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Curso de Java"))
                .andExpect(jsonPath("$.categoriaId").value(outraCategoria.toString()));
        mvc.perform(delete("/eventos/" + id).header("Authorization", authorization))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/eventos/" + id).header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404ParaEventoInexistente() throws Exception {
        String path = "/eventos/" + UUID.randomUUID();
        mvc.perform(get(path).header("Authorization", authorization))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.path").value(path));
        mvc.perform(put(path).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(dados(categoriaId)))
                .andExpect(status().isNotFound());
        mvc.perform(delete(path).header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404ParaCategoriaInexistente() throws Exception {
        String id = criarEvento();
        String body = dados(UUID.randomUUID());
        mvc.perform(post("/eventos").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
        mvc.perform(put("/eventos/" + id).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
        mvc.perform(get("/eventos/" + id).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriaId").value(categoriaId.toString()));
    }

    @Test
    void deveRetornar400ParaPeriodoInvalido() throws Exception {
        String id = criarEvento();
        for (String fim : new String[]{"2030-11-10T09:00:00", "2030-11-10T08:00:00"}) {
            String body = dados(categoriaId).replace("2030-11-10T12:00:00", fim);
            mvc.perform(post("/eventos").header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("O fim do evento deve ser posterior ao início"));
            mvc.perform(put("/eventos/" + id).header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deveValidarCamposDosDtos() throws Exception {
        String id = criarEvento();
        String valido = dados(categoriaId);
        String[] invalidos = {
                "{}",
                valido.replace("Semana acadêmica", "   "),
                valido.replace("Semana acadêmica", "a".repeat(256)),
                valido.replace("Encontro dos estudantes", ""),
                valido.replace("Encontro dos estudantes", "a".repeat(2001)),
                valido.replace("\"2030-11-10T09:00:00\"", "null"),
                valido.replace("\"2030-11-10T12:00:00\"", "null"),
                valido.replace("\"capacidade\":50", "\"capacidade\":0"),
                valido.replace("\"" + categoriaId + "\"", "null")
        };
        for (String body : invalidos) {
            mvc.perform(post("/eventos").header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/eventos/" + id).header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void deveExigirTokenEmTodasAsOperacoes() throws Exception {
        String path = "/eventos/" + UUID.randomUUID();
        mvc.perform(get("/eventos")).andExpect(status().isUnauthorized());
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post("/eventos").contentType(MediaType.APPLICATION_JSON).content(dados(categoriaId)))
                .andExpect(status().isUnauthorized());
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(dados(categoriaId)))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveDocumentarOperacoesRespostasEAutenticacao() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/eventos'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/eventos'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/eventos'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/eventos'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/eventos'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/eventos/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/eventos/{id}'].put.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/eventos/{id}'].delete.responses['204']").exists());
    }
}
