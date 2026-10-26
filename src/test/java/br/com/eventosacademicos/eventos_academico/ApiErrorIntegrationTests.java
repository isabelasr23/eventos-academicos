package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import br.com.eventosacademicos.eventos_academico.service.impl.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiErrorIntegrationTests {

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JwtService jwtService;

    private String authorization;

    @BeforeEach
    void autenticar() {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        authorization = "Bearer " + jwtService.generateToken(usuario);
    }

    @Test
    void deveRetornar400ParaJsonMalformado() throws Exception {
        mvc.perform(post("/categorias").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{json-invalido}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveRetornar400ParaUuidInvalido() throws Exception {
        mvc.perform(get("/eventos/id-invalido").header("Authorization", authorization))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/eventos/id-invalido"));
    }

    @Test
    void deveRetornar405ParaMetodoNaoPermitido() throws Exception {
        mvc.perform(put("/inscricoes").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Método HTTP não permitido"));
    }

    @Test
    void deveRetornar404ParaRotaInexistente() throws Exception {
        mvc.perform(get("/rota-inexistente").header("Authorization", authorization))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rota não encontrada"));
    }
}
