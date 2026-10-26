package br.com.eventosacademicos.eventos_academico.service.impl;

import br.com.eventosacademicos.eventos_academico.exception.EventoNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.PeriodoEventoInvalidoException;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.model.Evento;
import br.com.eventosacademicos.eventos_academico.repository.EventoRepository;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
import br.com.eventosacademicos.eventos_academico.service.EventoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.eventosacademicos.eventos_academico.repository.InscricaoRepository;
import br.com.eventosacademicos.eventos_academico.exception.EventoEmUsoException;
import br.com.eventosacademicos.eventos_academico.exception.CapacidadeEventoInvalidaException;

import java.util.List;
import java.util.UUID;

@Service
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaService categoriaService;
    private final InscricaoRepository inscricaoRepository;

    public EventoServiceImpl(
            EventoRepository eventoRepository,
            CategoriaService categoriaService,
            InscricaoRepository inscricaoRepository
    ) {
        this.eventoRepository = eventoRepository;
        this.categoriaService = categoriaService;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public Evento create(Evento evento) {
        validarPeriodo(evento);

        Categoria categoria = categoriaService.findById(evento.getCategoria().getId());
        evento.setCategoria(categoria);

        return eventoRepository.save(evento);
    }

    @Override
    public List<Evento> findAll() {
        return eventoRepository.findAll();
    }

    @Override
    public Evento findById(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException(id));
    }

    @Override
    @Transactional
    public Evento update(UUID id, Evento evento) {
        Evento eventoExistente = eventoRepository.findByIdComBloqueio(id)
                .orElseThrow(() -> new EventoNotFoundException(id));

        validarPeriodo(evento);
        Categoria categoria = categoriaService.findById(evento.getCategoria().getId());

        if (evento.getCapacidade() < inscricaoRepository.countByEventoId(id)) {
            throw new CapacidadeEventoInvalidaException();
        }

        eventoExistente.setTitulo(evento.getTitulo());
        eventoExistente.setDescricao(evento.getDescricao());
        eventoExistente.setInicio(evento.getInicio());
        eventoExistente.setFim(evento.getFim());
        eventoExistente.setCapacidade(evento.getCapacidade());
        eventoExistente.setCategoria(categoria);

        return eventoRepository.save(eventoExistente);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Evento evento = eventoRepository.findByIdComBloqueio(id)
                .orElseThrow(() -> new EventoNotFoundException(id));

        if (inscricaoRepository.existsByEventoId(id)) {
            throw new EventoEmUsoException(id);
        }

        eventoRepository.delete(evento);
    }

    private void validarPeriodo(Evento evento) {
        if (evento.getInicio() == null || evento.getFim() == null
                || !evento.getFim().isAfter(evento.getInicio())) {
            throw new PeriodoEventoInvalidoException();
        }
    }
}
