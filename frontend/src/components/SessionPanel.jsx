import { useEffect, useState } from 'react';
import CategoriesPanel from './CategoriesPanel.jsx';
import EventsPanel from './EventsPanel.jsx';
import OverviewPanel from './OverviewPanel.jsx';
import RegistrationsPanel from './RegistrationsPanel.jsx';
import UsersPanel from './UsersPanel.jsx';

const pages = {
  inicio: ['Visão geral', 'Conhecimento que conecta pessoas e transforma caminhos.'],
  eventos: ['Eventos', 'Encontre seu próximo encontro ou organize uma nova experiência.'],
  inscricoes: ['Minhas inscrições', 'Acompanhe os encontros dos quais você faz parte.'],
  categorias: ['Categorias', 'Organize cada tipo de encontro no seu lugar.'],
  usuarios: ['Usuários', 'Consulte e gerencie os participantes da comunidade.'],
};

function currentPage() { return Object.hasOwn(pages, window.location.hash.slice(1)) ? window.location.hash.slice(1) : 'inicio'; }

export default function SessionPanel({ session, onExpired }) {
  const [page, setPage] = useState(currentPage);
  useEffect(() => {
    const change = () => setPage(currentPage());
    window.addEventListener('hashchange', change);
    return () => window.removeEventListener('hashchange', change);
  }, []);
  function navigate(next) { window.location.hash = next; }
  const props = { token: session.token, onExpired, navigate };
  return (
    <section className="workspace" aria-labelledby="session-title">
      <nav className="workspace-nav" aria-label="Navegação principal">{Object.entries(pages).map(([key, [label]]) =>
        <a key={key} href={`#${key}`} aria-current={page === key ? 'page' : undefined}>{label}</a>)}</nav>
      <div className="workspace-heading">
        <div><span className="eyebrow">SUA COMUNIDADE ACADÊMICA</span><h1 id="session-title">{pages[page][0]}</h1><p>{pages[page][1]}</p></div>
        <span className="account-label">{session.email}</span>
      </div>
      {page === 'inicio' && <OverviewPanel {...props} />}
      {page === 'categorias' && <CategoriesPanel token={session.token} onExpired={onExpired} />}
      {page === 'eventos' && <EventsPanel {...props} />}
      {page === 'inscricoes' && <RegistrationsPanel {...props} />}
      {page === 'usuarios' && <UsersPanel session={session} onExpired={onExpired} />}
    </section>
  );
}
