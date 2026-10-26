package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import br.com.eventosacademicos.eventos_academico.service.impl.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtSecurityIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Value("${security.jwt.secret}")
    private String secret;

    @Test
    void deveExigirTokenEmTodosOsVerbosDeUsuarios() throws Exception {
        String path = "/usuarios/" + UUID.randomUUID();
        mvc.perform(get("/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirTokenValidoSemCriarSessao() throws Exception {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        String token = jwtService.generateToken(usuario);

        var result = mvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("maria@example.com"))
                .andExpect(jsonPath("$[0].senha").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
        mvc.perform(get("/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarTokenInvalidoVazioOuComAssinaturaDiferente() throws Exception {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        String outraAssinatura = new JwtService("outra-chave-exclusiva-para-testes-com-mais-de-32-bytes")
                .generateToken(usuario);

        for (String header : new String[]{"Bearer invalido", "Bearer ", "Basic abc", "Bearer " + outraAssinatura}) {
            mvc.perform(get("/usuarios").header("Authorization", header))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void deveRecusarTokenExpirado() throws Exception {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        String token = Jwts.builder()
                .subject(usuario.getEmail())
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarTokenDeUsuarioExcluido() throws Exception {
        Usuario usuario = usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));
        String token = jwtService.generateToken(usuario);
        usuarioService.delete(usuario.getId());

        mvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveManterSwaggerPublicoEDocumentarBearer() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs").header("Authorization", "Bearer expirado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/usuarios'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/usuarios'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").doesNotExist());
    }
}
