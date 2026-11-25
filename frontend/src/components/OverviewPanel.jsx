import useApiData from '../hooks/useApiData.js';
import { getEventWorkspace } from '../services/api.js';
import { EmptyState, Feedback, formatDate, LoadingState } from './WorkspaceUI.jsx';

export default function OverviewPanel({ token, onExpired, navigate }) {
  const resource = useApiData(getEventWorkspace, token, onExpired);
  const { eventos = [], categorias = [], inscricoes = [] } = resource.data || {};
  const upcoming = eventos.filter((event) => new Date(event.inicio) > new Date())
    .sort((a, b) => a.inicio.localeCompare(b.inicio)).slice(0, 4);
  return <>
    <Feedback error={resource.error} />
    {resource.loading ? <LoadingState /> : resource.data && <>
      <div className="stats-grid">
        {[['Eventos', eventos.length, 'eventos'], ['Minhas inscrições', inscricoes.length, 'inscricoes'], ['Categorias', categorias.length, 'categorias']]
          .map(([label, count, page]) => <button className="stat-card" key={page} onClick={() => navigate(page)}>
            <span>{label}</span><strong>{count}</strong><span className="stat-caption">Ver {label.toLowerCase()}</span>
          </button>)}
      </div>
      <section className="category-list-card overview-events">
        <div className="list-heading"><div><h2>Próximos encontros</h2><p className="section-description">Encontre uma oportunidade para aprender.</p></div>
          <button className="secondary-button" onClick={() => navigate('eventos')}>Ver eventos</button></div>
        {upcoming.length ? <ul className="upcoming-list">{upcoming.map((event) => <li key={event.id}>
          <span className="category-glyph" aria-hidden="true">▦</span><div><strong>{event.titulo}</strong><span>{formatDate(event.inicio)}</span></div>
          <span className="event-badge">{categorias.find((category) => category.id === event.categoriaId)?.nome || 'Evento'}</span>
        </li>)}</ul> : <EmptyState title="Novos encontros começam aqui." action={<button className="secondary-button" onClick={() => navigate('eventos')}>Gerenciar eventos</button>}>
          Ainda não há eventos futuros cadastrados.
        </EmptyState>}
      </section>
    </>}
    {resource.error && <button className="secondary-button" onClick={resource.reload} disabled={resource.loading}>Tentar novamente</button>}
  </>;
}
