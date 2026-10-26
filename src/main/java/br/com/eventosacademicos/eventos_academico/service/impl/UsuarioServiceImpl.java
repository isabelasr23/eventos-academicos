package br.com.eventosacademicos.eventos_academico.service.impl;

import br.com.eventosacademicos.eventos_academico.exception.EmailAlreadyExistsException;
import br.com.eventosacademicos.eventos_academico.exception.UsuarioNotFoundException;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.eventosacademicos.eventos_academico.repository.InscricaoRepository;
import br.com.eventosacademicos.eventos_academico.exception.UsuarioEmUsoException;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final InscricaoRepository inscricaoRepository;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            InscricaoRepository inscricaoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public Usuario create(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new EmailAlreadyExistsException(usuario.getEmail());
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario findById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id)) ;
    }

    @Override
    public Usuario update(UUID id, Usuario usuario) {

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (usuarioRepository.existsByEmailAndIdNot(usuario.getEmail(), id)) {
            throw new EmailAlreadyExistsException(usuario.getEmail());
        }

        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setSenha(passwordEncoder.encode(usuario.getSenha()));

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        Usuario usuario = usuarioRepository.findByIdComBloqueio(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (inscricaoRepository.existsByUsuarioId(id)) {
            throw new UsuarioEmUsoException(id);
        }

        usuarioRepository.delete(usuario);
    }
}
