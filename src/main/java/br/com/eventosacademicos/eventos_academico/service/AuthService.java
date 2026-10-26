package br.com.eventosacademicos.eventos_academico.service;

import br.com.eventosacademicos.eventos_academico.dto.request.CreateUsuarioRequestDto;
import br.com.eventosacademicos.eventos_academico.dto.request.LoginRequestDto;

public interface AuthService {
    String login(LoginRequestDto request);

    String register(CreateUsuarioRequestDto request);
}
