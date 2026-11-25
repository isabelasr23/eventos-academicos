const SESSION_KEY = 'eventos-academicos-session';

export function readSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(SESSION_KEY));
    return typeof session?.token === 'string' && session.token && typeof session.email === 'string'
      ? session
      : null;
  } catch {
    return null;
  }
}

export function saveSession(session) {
  try {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  } catch {
    // O acesso continua em memória se o navegador bloquear armazenamento.
  }
}

export function clearSession() {
  try {
    sessionStorage.removeItem(SESSION_KEY);
  } catch {
    // Não impede o encerramento da sessão em memória.
  }
}
