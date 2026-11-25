import { useEffect, useRef, useState } from 'react';
import { authenticate } from '../services/api.js';

export default function AuthForm({ mode, onModeChange, onAuthenticated, notice }) {
  const registering = mode === 'register';
  const [values, setValues] = useState({ nome: '', email: '', senha: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState('');
  const controllerRef = useRef(null);

  useEffect(() => () => controllerRef.current?.abort(), []);

  function update(event) {
    setValues((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    const email = values.email.trim();
    const nome = values.nome.trim();
    if (registering && !nome) {
      setError('Informe seu nome.');
      return;
    }
    if (new TextEncoder().encode(values.senha).length > 72) {
      setError('A senha é muito longa. Use uma senha mais curta.');
      return;
    }

    const controller = new AbortController();
    controllerRef.current = controller;
    setPending(true);
    try {
      const body = registering ? { nome, email, senha: values.senha } : { email, senha: values.senha };
      const token = await authenticate(mode, body, controller.signal);
      onAuthenticated({ token, email });
    } catch (failure) {
      if (failure.name !== 'AbortError') setError(failure.message);
    } finally {
      if (!controller.signal.aborted) setPending(false);
    }
  }

  return (
    <section className="auth-card" aria-labelledby="auth-title">
      <div className="mode-switch" aria-label="Escolha como acessar">
        <button type="button" className={!registering ? 'selected' : ''} aria-pressed={!registering}
          disabled={pending} onClick={() => onModeChange('login')}>Entrar</button>
        <button type="button" className={registering ? 'selected' : ''} aria-pressed={registering}
          disabled={pending} onClick={() => onModeChange('register')}>Criar conta</button>
      </div>
      <div className="card-heading">
        <span className="eyebrow">SEU ESPAÇO NO CAMPUS</span>
        <h2 id="auth-title">{registering ? 'Faça parte.' : 'Que bom ter você aqui.'}</h2>
        <p>{registering ? 'Crie sua conta para começar a participar.' : 'Entre para continuar sua jornada acadêmica.'}</p>
      </div>
      {notice && <p className="notice" role="status">{notice}</p>}
      <form onSubmit={submit} aria-busy={pending}>
        <fieldset disabled={pending}>
          {registering && <label htmlFor="nome">Nome completo
            <input id="nome" name="nome" autoComplete="name" value={values.nome} onChange={update}
              placeholder="Como podemos chamar você?" maxLength={255} required />
          </label>}
          <label htmlFor="email">E-mail
            <input id="email" name="email" type="email" autoComplete="email" value={values.email}
              onChange={update} placeholder="voce@exemplo.com" maxLength={255} required />
          </label>
          <label htmlFor="senha">Senha
            <span className="password-input">
              <input id="senha" name="senha" type={showPassword ? 'text' : 'password'}
                autoComplete={registering ? 'new-password' : 'current-password'} value={values.senha}
                onChange={update} placeholder={registering ? 'Pelo menos 6 caracteres' : 'Digite sua senha'}
                minLength={registering ? 6 : 1} maxLength={72} required />
              <button type="button" className="password-toggle" aria-controls="senha"
                aria-pressed={showPassword} onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? 'Ocultar' : 'Mostrar'}
              </button>
            </span>
          </label>
          {error && <p className="error-message" role="alert">{error}</p>}
          <button type="submit" className="primary-button">
            {pending ? 'Aguarde…' : registering ? 'Criar minha conta' : 'Entrar na minha conta'}
          </button>
        </fieldset>
      </form>
      <p className="card-footnote">Aprendizado, encontros e novas possibilidades.</p>
    </section>
  );
}
