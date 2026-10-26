package br.com.eventosacademicos.eventos_academico.service.impl;

import br.com.eventosacademicos.eventos_academico.exception.CategoriaNotFoundException;
import br.com.eventosacademicos.eventos_academico.exception.CategoriaEmUsoException;
import br.com.eventosacademicos.eventos_academico.exception.NomeCategoriaAlreadyExistsException;
import br.com.eventosacademicos.eventos_academico.model.Categoria;
import br.com.eventosacademicos.eventos_academico.repository.CategoriaRepository;
import br.com.eventosacademicos.eventos_academico.repository.EventoRepository;
import br.com.eventosacademicos.eventos_academico.service.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EventoRepository eventoRepository;

    public CategoriaServiceImpl(
            CategoriaRepository categoriaRepository,
            EventoRepository eventoRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public Categoria create(Categoria categoria) {

        if (categoriaRepository.existsByNome(categoria.getNome())) {
            throw new NomeCategoriaAlreadyExistsException(categoria.getNome());
        }

        return categoriaRepository.save(categoria);
    }

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria findById(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    @Override
    public Categoria update(UUID id, Categoria categoria) {

        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        if (categoriaRepository.existsByNomeAndIdNot(categoria.getNome(), id)) {
            throw new NomeCategoriaAlreadyExistsException(categoria.getNome());
        }

        categoriaExistente.setNome(categoria.getNome());

        return categoriaRepository.save(categoriaExistente);
    }

    @Override
    public void delete(UUID id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        if (eventoRepository.existsByCategoriaId(id)) {
            throw new CategoriaEmUsoException(id);
        }

        categoriaRepository.delete(categoria);
    }
}
