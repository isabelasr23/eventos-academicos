package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.impl.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {

    private static final String SECRET = "chave-exclusiva-para-testes-com-pelo-menos-32-bytes";
    private final JwtService jwtService = new JwtService(SECRET);
    private final Usuario usuario = new Usuario("Maria", "maria@example.com", "hash-da-senha");

    @Test
    void deveGerarTokenComEmailEValidadeDeUmaHora() {
        String token = jwtService.generateToken(usuario);
        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(jwtService.extractUsername(token)).isEqualTo(usuario.getEmail());
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(1000 * 60 * 60);
        assertThat(claims).doesNotContainKeys("senha", "password");
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }

    @Test
    void deveRejeitarTokenDeOutroUsuario() {
        String token = jwtService.generateToken(usuario);
        Usuario outro = new Usuario("Ana", "ana@example.com", "hash-da-senha");

        assertThat(jwtService.isTokenValid(token, outro)).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        String token = Jwts.builder()
                .subject(usuario.getEmail())
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(jwtService.isTokenValid(token, usuario)).isFalse();
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        JwtService outroServico = new JwtService("outra-chave-exclusiva-para-testes-com-mais-de-32-bytes");
        String token = outroServico.generateToken(usuario);

        assertThat(jwtService.isTokenValid(token, usuario)).isFalse();
    }

    @Test
    void deveRejeitarTokenMalformadoOuVazio() {
        assertThat(jwtService.isTokenValid("token-invalido", usuario)).isFalse();
        assertThat(jwtService.isTokenValid("", usuario)).isFalse();
        assertThat(jwtService.isTokenValid(null, usuario)).isFalse();
    }

    @Test
    void deveRejeitarTokenSemExpiracao() {
        String token = Jwts.builder()
                .subject(usuario.getEmail())
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(jwtService.isTokenValid(token, usuario)).isFalse();
    }

    @Test
    void deveRejeitarChaveCurta() {
        assertThatThrownBy(() -> new JwtService("chave-curta"))
                .isInstanceOf(WeakKeyException.class);
    }
}
