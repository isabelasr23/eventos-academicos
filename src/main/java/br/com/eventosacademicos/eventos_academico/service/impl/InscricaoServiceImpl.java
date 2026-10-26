package br.com.eventosacademicos.eventos_academico.service.impl;

import br.com.eventosacademicos.eventos_academico.exception.EventoNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.InscricaoInvalidaException;
import br.com.eventosacademicos.eventos_academico.exception.InscricaoNaoPermitidaException;
import br.com.eventosacademicos.eventos_academico.exception.InscricaoNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.UsuarioNotFoundException;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.model.Inscricao;
import br.com.eventosacademicos.eventos_academico.model.Usuario;
import br.com.eventosacademicos.eventos_academico.repository.EventoRepository;
import br.com.eventosacademicos.eventos_academico.repository.InscricaoRepository;
import br.com.eventosacademicos.eventos_academico.repository.UsuarioRepository;
import br.com.eventosacademicos.eventos_academico.service.InscricaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InscricaoServiceImpl implements InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public InscricaoServiceImpl(
            InscricaoRepository inscricaoRepository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Inscricao create(Inscricao inscricao, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findByIdComBloqueio(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));
        UUID eventoId = inscricao.getEvento().getId();
        Evento evento = eventoRepository.findByIdComBloqueio(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));

        if (inscricaoRepository.existsByUsuarioIdAndEventoId(usuarioId, eventoId)) {
            throw new InscricaoInvalidaException("Usuário já inscrito neste evento");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (!evento.getInicio().isAfter(agora)) {
            throw new InscricaoInvalidaException("As inscrições encerraram para este evento");
        }

        if (inscricaoRepository.countByEventoId(eventoId) >= evento.getCapacidade()) {
            throw new InscricaoInvalidaException("Evento sem vagas disponíveis");
        }

        inscricao.setUsuario(usuario);
        inscricao.setEvento(evento);
        inscricao.setDataInscricao(agora);

        return inscricaoRepository.save(inscricao);
    }

    @Override
    public List<Inscricao> findAll(UUID usuarioId) {
        return inscricaoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Inscricao findById(UUID id, UUID usuarioId) {
        Inscricao inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new InscricaoNotFoundException(id));

        if (!inscricao.getUsuario().getId().equals(usuarioId)) {
            throw new InscricaoNaoPermitidaException();
        }

        return inscricao;
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID usuarioId) {
        Inscricao inscricao = findById(id, usuarioId);

        inscricaoRepository.delete(inscricao);
    }
}
