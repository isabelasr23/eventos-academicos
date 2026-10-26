package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthenticationIntegrationTests {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void deveAutenticarUsuarioDoBancoPorEmailESenha() {
        Usuario criado = usuarioService.create(
                new Usuario("Maria", "maria@example.com", "senha123")
        );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("maria@example.com", "senha123")
        );

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("maria@example.com");
        Usuario autenticado = (Usuario) authentication.getPrincipal();
        assertThat(autenticado.getId()).isEqualTo(criado.getId());
    }

    @Test
    void deveRejeitarSenhaIncorreta() {
        usuarioService.create(new Usuario("Maria", "maria@example.com", "senha123"));

        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("maria@example.com", "senhaErrada")
        )).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deveRejeitarAutenticacaoDeEmailNaoCadastrado() {
        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("inexistente@example.com", "senha123")
        )).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deveInformarQuandoUsuarioNaoForEncontradoNaBusca() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inexistente@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
