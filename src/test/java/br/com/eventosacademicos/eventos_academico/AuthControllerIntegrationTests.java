package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import br.com.eventosacademicos.eventos_academico.service.impl.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTests {

    private static final String CADASTRO = """
            {"nome":"Maria","email":"maria@example.com","senha":"senha123"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void deveCadastrarComHashERetornarTokenValido() throws Exception {
        String response = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        var usuario = usuarioRepository.findByEmail("maria@example.com").orElseThrow();
        String token = JsonPath.read(response, "$.token");
        assertThat(passwordEncoder.matches("senha123", usuario.getSenha())).isTrue();
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }

    @Test
    void deveFazerLoginERetornarTokenValido() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated());

        String response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(response, "$.token");
        var usuario = usuarioRepository.findByEmail("maria@example.com").orElseThrow();
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }

    @Test
    void deveRetornar401ParaSenhaIncorretaOuEmailInexistente() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated());

        for (String email : new String[]{"maria@example.com", "inexistente@example.com"}) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"senha\":\"senhaErrada\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
        }
    }

    @Test
    void deveRecusarEmailDuplicado() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isConflict());
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    @Test
    void deveValidarDadosDeCadastroELogin() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"senha\":\"123\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalido\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());
        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void deveDocumentarCadastroELogin() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/auth/register'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/auth/login'].post.responses['401']").exists());
    }
}
