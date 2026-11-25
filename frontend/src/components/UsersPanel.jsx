import { useState } from 'react';
import useApiData from '../hooks/useApiData.js';
import useApiAction from '../hooks/useApiAction.js';
import { createUsuario, deleteUsuario, getUsuarios, updateUsuario } from '../services/api.js';
import { ConfirmModal, EmptyState, Feedback, LoadingState, Modal } from './WorkspaceUI.jsx';

function UserForm({ user, action, onSave, onDismiss }) {
  const [values, setValues] = useState({ nome: user?.nome || '', email: user?.email || '', senha: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [validation, setValidation] = useState('');
  function change(e) { setValues((current) => ({ ...current, [e.target.name]: e.target.value })); }
  function submit(e) {
    e.preventDefault(); setValidation('');
    if (!values.nome.trim()) { setValidation('Informe o nome do usuário.'); return; }
    if (new TextEncoder().encode(values.senha).length > 72) { setValidation('A senha é muito longa. Use uma senha mais curta.'); return; }
    onSave({ nome: values.nome.trim(), email: values.email.trim(), senha: values.senha });
  }
  return <form onSubmit={submit} aria-busy={action.busy}><fieldset disabled={action.busy}>
    <label htmlFor="user-name">Nome completo<input id="user-name" name="nome" value={values.nome} onChange={change} maxLength={255} required autoComplete="off" /></label>
    <label htmlFor="user-email">E-mail<input id="user-email" name="email" type="email" value={values.email} onChange={change} maxLength={255} required autoComplete="off" /></label>
    <label htmlFor="user-password">{user ? 'Nova senha' : 'Senha'}<span className="password-input">
      <input id="user-password" name="senha" type={showPassword ? 'text' : 'password'} value={values.senha} onChange={change} minLength={6} maxLength={72} required autoComplete="new-password" />
      <button type="button" className="password-toggle" aria-controls="user-password" aria-pressed={showPassword} onClick={() => setShowPassword(!showPassword)}>{showPassword ? 'Ocultar' : 'Mostrar'}</button>
    </span></label>
    <p className="field-help">{user ? 'Para salvar a atualização, informe uma nova senha com pelo menos 6 caracteres.' : 'Use uma senha com pelo menos 6 caracteres.'}</p>
    <Feedback error={validation || action.error} />
    <div className="modal-actions"><button className="secondary-button" type="button" onClick={onDismiss}>Cancelar</button>
      <button className="primary-button compact-button" type="submit">{action.busy ? 'Salvando…' : user ? 'Salvar alterações' : 'Cadastrar usuário'}</button></div>
  </fieldset></form>;
}

export default function UsersPanel({ session, onExpired }) {
  const resource = useApiData(getUsuarios, session.token, onExpired);
  const action = useApiAction(onExpired);
  const [modal, setModal] = useState(null);
  const [search, setSearch] = useState('');
  const users = resource.data || [];
  const visible = users.filter((user) => `${user.nome} ${user.email}`.toLocaleLowerCase('pt-BR').includes(search.toLocaleLowerCase('pt-BR')))
    .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  const own = (user) => user?.email === session.email;
  function open(type, user = null) { action.clear(); setModal({ type, user }); }
  function dismiss() { if (!action.busy) { setModal(null); action.clear(); } }
  function save(body) {
    const self = own(modal.user);
    action.run((signal) => modal.user ? updateUsuario(modal.user.id, body, session.token, signal) : createUsuario(body, session.token, signal),
      modal.user ? 'Usuário atualizado com sucesso.' : 'Usuário cadastrado com sucesso.', () => {
        setModal(null);
        if (self) onExpired('Sua conta foi atualizada. Entre novamente com os dados atuais.');
        else resource.reload();
      });
  }
  function remove() {
    const self = own(modal.user);
    action.run((signal) => deleteUsuario(modal.user.id, session.token, signal), 'Usuário excluído com sucesso.', () => {
      setModal(null);
      if (self) onExpired('Sua conta foi excluída.');
      else resource.reload();
    });
  }
  const busy = resource.loading || action.busy;
  return <>
    {!modal && <Feedback error={action.error} message={action.message} />}
    <Feedback error={resource.error} />
    <div className="workspace-toolbar">
      <label className="search-label" htmlFor="user-search">Buscar usuário<input id="user-search" type="search" placeholder="Nome ou e-mail" value={search} onChange={(e) => setSearch(e.target.value)} /></label>
      <div className="toolbar-actions"><button className="secondary-button" disabled={busy} onClick={resource.reload}>Atualizar lista</button>
        <button className="primary-button compact-button" disabled={busy} onClick={() => open('form')}>Novo usuário</button></div>
    </div>
    {resource.loading ? <LoadingState /> : !visible.length ? <EmptyState title="Nenhum usuário encontrado.">Tente outra busca ou cadastre um novo usuário.</EmptyState>
      : <div className="users-list">{visible.map((user) => <article className="user-card" key={user.id}>
        <span className="user-avatar" aria-hidden="true">{user.nome.slice(0, 1).toUpperCase()}</span>
        <div className="user-copy"><h2>{user.nome} {own(user) && <span className="self-label">Você</span>}</h2><p>{user.email}</p></div>
        <div className="category-actions"><button className="text-button" aria-label={`Ver usuário ${user.nome}`} disabled={busy} onClick={() => open('details', user)}>Detalhes</button>
          <button className="text-button" aria-label={`Editar usuário ${user.nome}`} disabled={busy} onClick={() => open('form', user)}>Editar</button>
          <button className="text-button danger-button" aria-label={`Excluir usuário ${user.nome}`} disabled={busy} onClick={() => open('delete', user)}>Excluir</button></div>
      </article>)}</div>}
    {modal?.type === 'form' && <Modal title={modal.user ? 'Editar usuário' : 'Novo usuário'} onDismiss={dismiss} busy={action.busy}>
      {own(modal.user) && <p className="notice">Ao salvar sua conta, você precisará entrar novamente.</p>}
      <UserForm user={modal.user} action={action} onSave={save} onDismiss={dismiss} />
    </Modal>}
    {modal?.type === 'details' && <Modal title="Detalhes do usuário" onDismiss={dismiss}>
      <dl className="details-grid"><div><dt>Nome</dt><dd>{modal.user.nome}</dd></div><div><dt>E-mail</dt><dd>{modal.user.email}</dd></div>
        <div className="full-width"><dt>Identificação</dt><dd className="record-code">{modal.user.id}</dd></div></dl>
      <div className="modal-actions"><button className="secondary-button" onClick={dismiss}>Fechar detalhes</button></div>
    </Modal>}
    {modal?.type === 'delete' && <ConfirmModal title={own(modal.user) ? 'Excluir minha conta' : 'Excluir usuário'} confirmLabel="Confirmar exclusão" action={action} onDismiss={dismiss} onConfirm={remove}>
      <p>Excluir <strong>{modal.user.nome}</strong>? A exclusão é definitiva e só será permitida se não houver inscrições vinculadas.</p>
    </ConfirmModal>}
  </>;
}
