import { useEffect, useId, useRef } from 'react';

export function Feedback({ error, message }) {
  return <>{error && <p className="error-message" role="alert">{error}</p>}
    {message && <p className="notice success-notice" role="status">{message}</p>}</>;
}

export function Modal({ title, children, onDismiss, busy = false }) {
  const ref = useRef(null);
  const titleId = useId();
  useEffect(() => {
    const dialog = ref.current;
    dialog.showModal();
    return () => dialog.close();
  }, []);
  return <dialog ref={ref} className="modal" aria-labelledby={titleId}
    onCancel={(event) => { event.preventDefault(); if (!busy) onDismiss(); }}>
    <header className="modal-heading"><h2 id={titleId}>{title}</h2>
      <button type="button" className="text-button" disabled={busy} onClick={onDismiss} aria-label="Fechar janela">Fechar</button>
    </header>
    <div className="modal-content">{children}</div>
  </dialog>;
}

export function ConfirmModal({ title, children, confirmLabel, action, onConfirm, onDismiss }) {
  return <Modal title={title} onDismiss={onDismiss} busy={action.busy}>
    <div className="confirmation-description">{children}</div>
    <Feedback error={action.error} />
    <div className="modal-actions">
      <button type="button" className="secondary-button" disabled={action.busy} onClick={onDismiss}>Voltar</button>
      <button type="button" className="danger-confirm" disabled={action.busy} onClick={onConfirm}>
        {action.busy ? 'Aguarde…' : confirmLabel}</button>
    </div>
  </Modal>;
}

export function EmptyState({ title, children, action }) {
  return <div className="empty-categories"><span className="empty-symbol" aria-hidden="true">▦</span>
    <h3>{title}</h3><p>{children}</p>{action && <div className="empty-action">{action}</div>}</div>;
}

export function LoadingState() { return <p className="list-placeholder" role="status">Carregando dados…</p>; }

export function formatDate(value) {
  if (!value) return 'Não informado';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

export function EventInfo({ event, categoryName }) {
  return <><p className="event-description">{event.descricao}</p>
    <dl className="details-grid">
      <div><dt>Categoria</dt><dd>{categoryName || 'Categoria não disponível'}</dd></div>
      <div><dt>Capacidade</dt><dd>{event.capacidade} {event.capacidade === 1 ? 'participante' : 'participantes'}</dd></div>
      <div><dt>Início</dt><dd>{formatDate(event.inicio)}</dd></div>
      <div><dt>Fim</dt><dd>{formatDate(event.fim)}</dd></div>
    </dl></>;
}
