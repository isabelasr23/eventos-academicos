import { useState } from 'react';
import useApiData from '../hooks/useApiData.js';
import useApiAction from '../hooks/useApiAction.js';
import { createEvento, createInscricao, deleteEvento, getEventWorkspace, updateEvento } from '../services/api.js';
import { ConfirmModal, EmptyState, EventInfo, Feedback, formatDate, LoadingState, Modal } from './WorkspaceUI.jsx';

function EventForm({ event, categories, action, onSave, onDismiss }) {
  const [values, setValues] = useState(() => ({
    titulo: event?.titulo || '', descricao: event?.descricao || '',
    inicio: event?.inicio?.slice(0, 19) || '', fim: event?.fim?.slice(0, 19) || '',
    capacidade: event?.capacidade || '', categoriaId: event?.categoriaId || '',
  }));
  const [validation, setValidation] = useState('');
  function change(e) { setValues((current) => ({ ...current, [e.target.name]: e.target.value })); }
  function submit(e) {
    e.preventDefault(); setValidation('');
    if (!values.titulo.trim() || !values.descricao.trim()) { setValidation('Preencha título e descrição.'); return; }
    if (new Date(values.fim) <= new Date(values.inicio)) { setValidation('O fim do evento deve ser posterior ao início.'); return; }
    onSave({ ...values, titulo: values.titulo.trim(), descricao: values.descricao.trim(), capacidade: Number(values.capacidade) });
  }
  return <form onSubmit={submit} aria-busy={action.busy}><fieldset disabled={action.busy}>
    <label htmlFor="event-title">Título<input id="event-title" name="titulo" value={values.titulo} onChange={change} required maxLength={255} /></label>
    <label htmlFor="event-description">Descrição<textarea id="event-description" name="descricao" value={values.descricao} onChange={change} required maxLength={2000} rows={4} /></label>
    <div className="form-grid">
      <label htmlFor="event-start">Início<input id="event-start" name="inicio" type="datetime-local" step="1" value={values.inicio} onChange={change} required /></label>
      <label htmlFor="event-end">Fim<input id="event-end" name="fim" type="datetime-local" step="1" value={values.fim} onChange={change} required /></label>
      <label htmlFor="event-capacity">Capacidade<input id="event-capacity" name="capacidade" type="number" min="1" max="2147483647" step="1" value={values.capacidade} onChange={change} required /></label>
      <label htmlFor="event-category">Categoria<select id="event-category" name="categoriaId" value={values.categoriaId} onChange={change} required>
        <option value="">Selecione uma categoria</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.nome}</option>)}
      </select></label>
    </div>
    <Feedback error={validation || action.error} />
    <div className="modal-actions"><button type="button" className="secondary-button" onClick={onDismiss}>Cancelar</button>
      <button type="submit" className="primary-button compact-button">{action.busy ? 'Salvando…' : event ? 'Salvar alterações' : 'Cadastrar evento'}</button></div>
  </fieldset></form>;
}

export default function EventsPanel({ token, onExpired, navigate }) {
  const resource = useApiData(getEventWorkspace, token, onExpired);
  const action = useApiAction(onExpired);
  const [modal, setModal] = useState(null);
  const [search, setSearch] = useState('');
  const { eventos = [], categorias = [], inscricoes = [] } = resource.data || {};
  const mine = new Set(inscricoes.map((registration) => registration.eventoId));
  const visible = eventos.filter((event) => event.titulo.toLocaleLowerCase('pt-BR').includes(search.toLocaleLowerCase('pt-BR')))
    .sort((a, b) => a.inicio.localeCompare(b.inicio));
  function open(type, event = null) { action.clear(); setModal({ type, event }); }
  function dismiss() { if (!action.busy) { setModal(null); action.clear(); } }
  function save(body) {
    action.run((signal) => modal.event ? updateEvento(modal.event.id, body, token, signal) : createEvento(body, token, signal),
      modal.event ? 'Evento atualizado com sucesso.' : 'Evento cadastrado com sucesso.', () => { setModal(null); resource.reload(); });
  }
  function register(event) {
    action.run((signal) => createInscricao(event.id, token, signal), 'Inscrição realizada! Consulte “Minhas inscrições”.', () => {
      setModal(null); resource.reload();
    });
  }
  function remove() {
    action.run((signal) => deleteEvento(modal.event.id, token, signal), 'Evento excluído com sucesso.', () => { setModal(null); resource.reload(); });
  }
  const unavailable = action.busy || resource.loading;
  return <>
    {!modal && <Feedback error={action.error} message={action.message} />}
    <Feedback error={resource.error} />
    <div className="workspace-toolbar">
      <label className="search-label" htmlFor="event-search">Buscar evento<input id="event-search" type="search" placeholder="Digite o título" value={search} onChange={(e) => setSearch(e.target.value)} /></label>
      <div className="toolbar-actions"><button className="secondary-button" disabled={unavailable} onClick={resource.reload}>Atualizar lista</button>
        <button className="primary-button compact-button" disabled={unavailable || !categorias.length} onClick={() => open('form')}>Novo evento</button></div>
    </div>
    {!resource.loading && !resource.error && !categorias.length && <p className="notice">Cadastre uma categoria antes de criar eventos. <button className="text-button" onClick={() => navigate('categorias')}>Ir para categorias</button></p>}
    {resource.loading ? <LoadingState /> : !visible.length ? <EmptyState title={search ? 'Nenhum evento encontrado.' : 'Sua agenda começa aqui.'}>
      {search ? 'Tente buscar por outro título.' : 'Crie o primeiro evento para começar a receber inscrições.'}
    </EmptyState> : <div className="events-grid">{visible.map((event) => {
      const ended = new Date(event.inicio) <= new Date();
      return <article className="event-card" key={event.id}>
        <div className="event-card-top"><span className="event-badge">{categorias.find((category) => category.id === event.categoriaId)?.nome || 'Evento'}</span>
          <span className={`event-state ${ended ? 'muted-state' : ''}`}>{ended ? 'Inscrições encerradas' : 'Inscrições abertas'}</span></div>
        <h2>{event.titulo}</h2><p className="event-card-description">{event.descricao}</p>
        <dl className="event-summary"><div><dt>Início</dt><dd>{formatDate(event.inicio)}</dd></div><div><dt>Capacidade</dt><dd>{event.capacidade} {event.capacidade === 1 ? 'participante' : 'participantes'}</dd></div></dl>
        <div className="event-primary-actions"><button className="secondary-button" disabled={unavailable} onClick={() => open('details', event)}>Ver detalhes</button>
          <button className="primary-button compact-button" disabled={unavailable || ended || mine.has(event.id)} onClick={() => register(event)}>
            {mine.has(event.id) ? 'Você está inscrito' : 'Inscrever-me'}</button></div>
        <div className="event-admin-actions"><button className="text-button" aria-label={`Editar evento ${event.titulo}`} disabled={unavailable} onClick={() => open('form', event)}>Editar</button>
          <button className="text-button danger-button" aria-label={`Excluir evento ${event.titulo}`} disabled={unavailable} onClick={() => open('delete', event)}>Excluir</button></div>
      </article>;
    })}</div>}
    {modal?.type === 'form' && <Modal title={modal.event ? 'Editar evento' : 'Novo evento'} onDismiss={dismiss} busy={action.busy}>
      <EventForm event={modal.event} categories={categorias} action={action} onSave={save} onDismiss={dismiss} />
    </Modal>}
    {modal?.type === 'details' && <Modal title={modal.event.titulo} onDismiss={dismiss} busy={action.busy}>
      <EventInfo event={modal.event} categoryName={categorias.find((category) => category.id === modal.event.categoriaId)?.nome} />
      <Feedback error={action.error} />
      <div className="modal-actions"><button className="secondary-button" disabled={action.busy} onClick={dismiss}>Fechar detalhes</button>
        <button className="primary-button compact-button" disabled={action.busy || mine.has(modal.event.id) || new Date(modal.event.inicio) <= new Date()} onClick={() => register(modal.event)}>
          {mine.has(modal.event.id) ? 'Você está inscrito' : action.busy ? 'Inscrevendo…' : 'Inscrever-me'}</button></div>
    </Modal>}
    {modal?.type === 'delete' && <ConfirmModal title="Excluir evento" confirmLabel="Confirmar exclusão" action={action} onDismiss={dismiss} onConfirm={remove}>
      <p>Excluir <strong>{modal.event.titulo}</strong>? Eventos com inscrições não podem ser excluídos.</p>
    </ConfirmModal>}
  </>;
}
