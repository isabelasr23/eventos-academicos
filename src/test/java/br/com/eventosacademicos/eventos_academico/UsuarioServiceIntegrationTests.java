package br.com.eventosacademicos.eventos_academico;

import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UsuarioServiceIntegrationTests {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveSalvarSenhaComHashAoCadastrarUsuario() {
        Usuario usuario = new Usuario("Maria", "maria@example.com", "senha123");

        Usuario criado = usuarioService.create(usuario);
        Usuario salvo = usuarioRepository.findById(criado.getId()).orElseThrow();

        assertThat(salvo.getSenha()).isNotEqualTo("senha123");
        assertThat(passwordEncoder.matches("senha123", salvo.getSenha())).isTrue();
        assertThat(passwordEncoder.matches("senhaErrada", salvo.getSenha())).isFalse();
    }

    @Test
    void deveSalvarNovoHashAoAtualizarSenha() {
        Usuario criado = usuarioService.create(
                new Usuario("Maria", "maria@example.com", "senha123")
        );
        String hashAnterior = criado.getSenha();

        usuarioService.update(
                criado.getId(),
                new Usuario("Maria Silva", "maria@example.com", "novaSenha123")
        );

        Usuario salvo = usuarioRepository.findById(criado.getId()).orElseThrow();

        assertThat(salvo.getSenha()).isNotEqualTo("novaSenha123");
        assertThat(salvo.getSenha()).isNotEqualTo(hashAnterior);
        assertThat(passwordEncoder.matches("novaSenha123", salvo.getSenha())).isTrue();
        assertThat(passwordEncoder.matches("senha123", salvo.getSenha())).isFalse();
    }
}
