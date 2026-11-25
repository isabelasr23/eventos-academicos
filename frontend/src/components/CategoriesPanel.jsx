import { useEffect, useRef, useState } from 'react';
import { createCategoria, deleteCategoria, getCategorias, updateCategoria } from '../services/api.js';

export default function CategoriesPanel({ token, onExpired }) {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [actionError, setActionError] = useState('');
  const [formError, setFormError] = useState('');
  const [message, setMessage] = useState('');
  const [attempt, setAttempt] = useState(0);
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [pending, setPending] = useState(null);
  const inputRef = useRef(null);
  const mutationRef = useRef(null);
  const busy = pending !== null;

  useEffect(() => () => mutationRef.current?.abort(), []);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setLoadError('');
    getCategorias(token, controller.signal)
      .then(setCategories)
      .catch((failure) => {
        if (failure.name === 'AbortError') return;
        if (failure.status === 401) onExpired();
        else setLoadError(failure.message);
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [token, onExpired, attempt]);

  function resetForm() {
    setEditingId(null);
    setName('');
    setFormError('');
  }

  function edit(category) {
    setEditingId(category.id);
    setName(category.nome);
    setFormError('');
    setActionError('');
    setMessage('');
    setDeleting(null);
    inputRef.current?.focus();
  }

  async function save(event) {
    event.preventDefault();
    const nome = name.trim();
    setFormError('');
    setActionError('');
    setMessage('');
    if (!nome) {
      setFormError('Informe o nome da categoria.');
      inputRef.current?.focus();
      return;
    }
    const controller = new AbortController();
    mutationRef.current = controller;
    setPending('save');
    try {
      const saved = editingId
        ? await updateCategoria(editingId, nome, token, controller.signal)
        : await createCategoria(nome, token, controller.signal);
      setCategories((current) => editingId
        ? current.map((category) => category.id === editingId ? saved : category)
        : [...current, saved]);
      setMessage(editingId ? 'Categoria atualizada com sucesso.' : 'Categoria cadastrada com sucesso.');
      resetForm();
      setDeleting(null);
    } catch (failure) {
      if (failure.name === 'AbortError') return;
      if (failure.status === 401) onExpired();
      else setFormError(failure.message);
    } finally {
      if (!controller.signal.aborted) setPending(null);
    }
  }

  async function remove() {
    const controller = new AbortController();
    mutationRef.current = controller;
    setPending('delete');
    setFormError('');
    setMessage('');
    setActionError('');
    try {
      await deleteCategoria(deleting.id, token, controller.signal);
      setCategories((current) => current.filter((category) => category.id !== deleting.id));
      if (editingId === deleting.id) resetForm();
      setMessage('Categoria excluída com sucesso.');
      setDeleting(null);
    } catch (failure) {
      if (failure.name === 'AbortError') return;
      if (failure.status === 401) onExpired();
      else {
        setActionError(failure.message);
        setDeleting(null);
      }
    } finally {
      if (!controller.signal.aborted) setPending(null);
    }
  }

  const sorted = [...categories].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));

  return (
    <div className="categories-panel">
      {message && <p className="notice success-notice" role="status">{message}</p>}
      {actionError && <p className="error-message" role="alert">{actionError}</p>}
      <div className="category-workspace">
        <section className="category-form-card" aria-labelledby="category-form-title">
          <span className="section-symbol" aria-hidden="true">{editingId ? '✎' : '+'}</span>
          <h2 id="category-form-title">{editingId ? 'Editar categoria' : 'Nova categoria'}</h2>
          <p className="section-description">Organize os eventos por tipo de encontro.</p>
          <form onSubmit={save} aria-busy={pending === 'save'}>
            <fieldset disabled={busy || loading}>
              <label htmlFor="category-name">Nome da categoria
                <input ref={inputRef} id="category-name" value={name} onChange={(event) => setName(event.target.value)}
                  placeholder="Ex.: Palestra" maxLength={255} required
                  aria-describedby={formError ? 'category-form-error' : 'category-name-help'} />
              </label>
              <p className="field-help" id="category-name-help">Escolha um nome que identifique o tipo de evento.</p>
              {formError && <p className="error-message" id="category-form-error" role="alert">{formError}</p>}
              <button type="submit" className="primary-button">
                {pending === 'save' ? 'Salvando…' : editingId ? 'Salvar alterações' : 'Cadastrar categoria'}
              </button>
              {editingId && <button type="button" className="secondary-button cancel-edit" onClick={resetForm}>Cancelar edição</button>}
            </fieldset>
          </form>
        </section>
        <section className="category-list-card" aria-labelledby="category-list-title">
          <div className="list-heading">
            <div><h2 id="category-list-title">Categorias cadastradas</h2>
              <p className="section-description">{loading ? 'Consultando categorias…' : `${categories.length} ${categories.length === 1 ? 'categoria' : 'categorias'}`}</p>
            </div>
            <button type="button" className="secondary-button" disabled={busy || loading}
              onClick={() => setAttempt(attempt + 1)}>Atualizar lista</button>
          </div>
          {loadError && <p className="error-message" role="alert">{loadError}</p>}
          {loading ? <p className="list-placeholder" role="status">Carregando a lista…</p>
            : sorted.length === 0 ? <div className="empty-categories">
              <span className="empty-symbol" aria-hidden="true">▦</span>
              <h3>{loadError ? 'A lista não está disponível.' : 'Comece por uma categoria.'}</h3>
              <p>{loadError ? 'Use “Atualizar lista” para tentar novamente.' : 'Cadastre tipos como palestra, curso e congresso para organizar seus eventos.'}</p>
            </div> : <ul className="category-list">
              {sorted.map((category) => <li key={category.id} className={editingId === category.id ? 'editing-category' : ''}>
                <div className="category-row">
                  <span className="category-glyph" aria-hidden="true">▦</span>
                  <span className="category-name">{category.nome}</span>
                  <div className="category-actions">
                    <button type="button" className="text-button" disabled={busy || loading}
                      aria-label={`Editar ${category.nome}`} onClick={() => edit(category)}>Editar</button>
                    <button type="button" className="text-button danger-button" disabled={busy || loading}
                      aria-label={`Excluir ${category.nome}`} onClick={() => {
                        setDeleting(category); setActionError(''); setMessage('');
                      }}>Excluir</button>
                  </div>
                </div>
                {deleting?.id === category.id && <div className="delete-confirmation" role="group" aria-label={`Confirmar exclusão de ${category.nome}`}>
                  <p>Excluir <strong>{category.nome}</strong>? Essa ação não pode ser desfeita.</p>
                  <div className="confirmation-actions">
                    <button type="button" className="danger-confirm" disabled={busy} onClick={remove}>
                      {pending === 'delete' ? 'Excluindo…' : 'Confirmar exclusão'}</button>
                    <button type="button" className="secondary-button" disabled={busy} onClick={() => setDeleting(null)}>Manter categoria</button>
                  </div>
                </div>}
              </li>)}
            </ul>}
        </section>
      </div>
    </div>
  );
}
