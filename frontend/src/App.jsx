import { useCallback, useState } from 'react';
import AuthForm from './components/AuthForm.jsx';
import SessionPanel from './components/SessionPanel.jsx';
import { clearSession, readSession, saveSession } from './services/session.js';

export default function App() {
  const [mode, setMode] = useState('login');
  const [session, setSession] = useState(readSession);
  const [notice, setNotice] = useState('');

  function authenticate(nextSession) {
    saveSession(nextSession);
    setSession(nextSession);
    setNotice('');
  }

  function signOut() {
    clearSession();
    setSession(null);
    setMode('login');
    setNotice('Você saiu da sua conta.');
  }

  const expireSession = useCallback((message = 'Sua sessão expirou. Entre novamente para continuar.') => {
    clearSession();
    setSession(null);
    setMode('login');
    setNotice(message);
  }, []);

  return (
    <div className="app-shell">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Eventos Acadêmicos, início">
          <span className="brand-symbol" aria-hidden="true">ea<span>•</span></span>
          <span>Eventos<br /><strong>Acadêmicos</strong></span>
        </a>
        {session ? <button className="secondary-button" type="button" onClick={signOut}>Sair da conta</button>
          : <span className="header-label">Conectados pelo conhecimento</span>}
      </header>
      <main className={session ? 'main-layout workspace-layout' : 'main-layout'}>
        {!session && <section className="introduction" aria-labelledby="page-title">
          <span className="hero-kicker"><span aria-hidden="true">✳</span> SUA COMUNIDADE ACADÊMICA</span>
          <h1 id="page-title">Conhecimento<br />que <span>aproxima.</span></h1>
          <p className="hero-description">Encontros que despertam ideias. Experiências que fazem parte da sua formação.</p>
          <div className="event-art" aria-hidden="true">
            <div className="art-orbit"></div>
            <div className="art-ticket">
              <span className="ticket-label">CAMPUS EM MOVIMENTO</span>
              <div className="ticket-mark">↗</div>
              <strong>Ideias ganham<br />novos caminhos.</strong>
              <span className="ticket-line"></span>
              <span className="ticket-caption">Aprenda. Compartilhe. Participe.</span>
            </div>
            <span className="art-note">Uma conexão pode mudar tudo.</span>
          </div>
        </section>}
        {session ? <SessionPanel session={session} onExpired={expireSession} />
          : <AuthForm key={mode} mode={mode} onModeChange={setMode} onAuthenticated={authenticate} notice={notice} />}
      </main>
      <footer className="site-footer"><span>Eventos Acadêmicos</span><span>Um espaço para construir conexões.</span></footer>
    </div>
  );
}
