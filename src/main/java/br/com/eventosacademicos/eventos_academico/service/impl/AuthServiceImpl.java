package br.com.eventosacademicos.eventos_academico.service.impl;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.LoginRequestDto;
import br.com.eventosacademicos.eventos_academico.mapper.UsuarioMapper;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.service.AuthService;
import br.com.eventosacademicos.eventos_academico.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.jwtService = jwtService;
    }

    @Override
    public String login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return jwtService.generateToken(usuario);
    }

    @Override
    public String register(CreateUsuarioRequestDto request) {
        Usuario usuario = usuarioMapper.toEntity(request);

        Usuario usuarioCriado = usuarioService.create(usuario);

        return jwtService.generateToken(usuarioCriado);
    }
}
