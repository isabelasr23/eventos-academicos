package br.com.eventosacademicos.eventos_academico.service.impl;

import br.com.eventosacademicos.eventos_academico.exception.EmailAlreadyExistsException;
import br.com.eventosacademicos.eventos_academico.exception.UsuarioNotFoundException;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario create(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new EmailAlreadyExistsException(usuario.getEmail());
        }

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
        usuarioExistente.setSenha(usuario.getSenha());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void delete(UUID id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        usuarioRepository.delete(usuario);
    }
}
