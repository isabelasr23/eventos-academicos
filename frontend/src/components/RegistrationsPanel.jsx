import { useState } from 'react';
import useApiData from '../hooks/useApiData.js';
import useApiAction from '../hooks/useApiAction.js';
import { deleteInscricao, getInscricao, getRegistrationWorkspace } from '../services/api.js';
import { ConfirmModal, EmptyState, Feedback, formatDate, LoadingState, Modal } from './WorkspaceUI.jsx';

export default function RegistrationsPanel({ token, onExpired, navigate }) {
  const resource = useApiData(getRegistrationWorkspace, token, onExpired);
  const action = useApiAction(onExpired);
  const [modal, setModal] = useState(null);
  const { inscricoes = [], eventos = [] } = resource.data || {};
  const eventFor = (registration) => eventos.find((event) => event.id === registration.eventoId);
  const sorted = [...inscricoes].sort((a, b) => b.dataInscricao.localeCompare(a.dataInscricao));
  function dismiss() { if (!action.busy) { setModal(null); action.clear(); } }
  function details(registration) {
    action.run((signal) => getInscricao(registration.id, token, signal), '', (result) => setModal({ type: 'details', registration: result }));
  }
  function cancel() {
    action.run((signal) => deleteInscricao(modal.registration.id, token, signal), 'Inscrição cancelada. A vaga foi liberada.', () => { setModal(null); resource.reload(); });
  }
  const busy = action.busy || resource.loading;
  return <>
    {!modal && <Feedback error={action.error} message={action.message} />}
    <Feedback error={resource.error} />
    <div className="workspace-toolbar"><p className="section-description">{resource.loading ? 'Consultando inscrições…' : `${inscricoes.length} ${inscricoes.length === 1 ? 'inscrição' : 'inscrições'}`}</p>
      <div className="toolbar-actions"><button className="secondary-button" disabled={busy} onClick={resource.reload}>Atualizar lista</button>
        <button className="primary-button compact-button" onClick={() => navigate('eventos')}>Explorar eventos</button></div></div>
    {resource.loading ? <LoadingState /> : !sorted.length ? <EmptyState title="Seu próximo encontro está te esperando."
      action={<button className="secondary-button" onClick={() => navigate('eventos')}>Ver eventos disponíveis</button>}>
      Você ainda não tem inscrições. Escolha um evento e participe.
    </EmptyState> : <div className="registration-list">{sorted.map((registration) => {
      const event = eventFor(registration);
      return <article className="registration-card" key={registration.id}>
        <span className="registration-symbol" aria-hidden="true">✓</span>
        <div className="registration-copy"><span className="eyebrow">INSCRIÇÃO CONFIRMADA</span>
          <h2>{event?.titulo || 'Evento não disponível'}</h2>
          <p>{event ? formatDate(event.inicio) : 'Consulte os detalhes da inscrição.'}</p>
          <span className="registration-date">Inscrição realizada em {formatDate(registration.dataInscricao)}</span>
        </div>
        <div className="registration-actions"><button className="secondary-button" disabled={busy} onClick={() => details(registration)}>Ver inscrição</button>
          <button className="text-button danger-button" disabled={busy} aria-label={`Cancelar inscrição em ${event?.titulo || registration.id}`}
            onClick={() => { action.clear(); setModal({ type: 'cancel', registration }); }}>Cancelar inscrição</button></div>
      </article>;
    })}</div>}
    {modal?.type === 'details' && <Modal title="Detalhes da inscrição" onDismiss={dismiss}>
      <span className="event-badge">Inscrição confirmada</span>
      <h3 className="registration-detail-title">{eventFor(modal.registration)?.titulo || 'Evento não disponível'}</h3>
      <dl className="details-grid"><div><dt>Data da inscrição</dt><dd>{formatDate(modal.registration.dataInscricao)}</dd></div>
        <div><dt>Início do evento</dt><dd>{formatDate(eventFor(modal.registration)?.inicio)}</dd></div>
        <div className="full-width"><dt>Código da inscrição</dt><dd className="record-code">{modal.registration.id}</dd></div></dl>
      <div className="modal-actions"><button className="secondary-button" onClick={dismiss}>Fechar detalhes</button></div>
    </Modal>}
    {modal?.type === 'cancel' && <ConfirmModal title="Cancelar inscrição" confirmLabel="Confirmar cancelamento" action={action} onDismiss={dismiss} onConfirm={cancel}>
      <p>Cancelar sua inscrição em <strong>{eventFor(modal.registration)?.titulo || 'este evento'}</strong>? A vaga será liberada para outra pessoa.</p>
    </ConfirmModal>}
  </>;
}
