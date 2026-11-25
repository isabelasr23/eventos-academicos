export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function request(path, { method = 'GET', body, token, signal } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: 'omit',
      signal,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError('Não foi possível conectar ao servidor. Tente novamente.', 0);
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      if (response.ok) throw new ApiError('O servidor enviou uma resposta inesperada.', response.status);
    }
  }

  if (!response.ok) {
    const fallback = response.status === 401
      ? 'Sua sessão expirou. Entre novamente.'
      : response.status >= 500
        ? 'O servidor está indisponível no momento. Tente novamente.'
        : 'Não foi possível concluir a solicitação.';
    throw new ApiError(data?.message || fallback, response.status);
  }

  return data;
}

export async function authenticate(mode, values, signal) {
  const path = mode === 'register' ? '/auth/register' : '/auth/login';
  const data = await request(path, { method: 'POST', body: values, signal });
  if (typeof data?.token !== 'string' || !data.token) {
    throw new ApiError('O servidor não retornou o token de acesso.', 200);
  }
  return data.token;
}

export async function getCategorias(token, signal) {
  const data = await request('/categorias', { token, signal });
  if (!Array.isArray(data)) throw new ApiError('Não foi possível ler as categorias.', 200);
  return data;
}

export function createCategoria(nome, token, signal) {
  return request('/categorias', { method: 'POST', body: { nome }, token, signal });
}

export function updateCategoria(id, nome, token, signal) {
  return request(`/categorias/${encodeURIComponent(id)}`, {
    method: 'PUT', body: { nome }, token, signal,
  });
}

export function deleteCategoria(id, token, signal) {
  return request(`/categorias/${encodeURIComponent(id)}`, { method: 'DELETE', token, signal });
}

async function getList(path, token, signal) {
  const data = await request(path, { token, signal });
  if (!Array.isArray(data)) throw new ApiError('Não foi possível ler os dados recebidos.', 200);
  return data;
}

export const getEventos = (token, signal) => getList('/eventos', token, signal);
export const getInscricoes = (token, signal) => getList('/inscricoes', token, signal);
export const getUsuarios = (token, signal) => getList('/usuarios', token, signal);

export const createEvento = (body, token, signal) => request('/eventos', { method: 'POST', body, token, signal });
export const updateEvento = (id, body, token, signal) => request(`/eventos/${encodeURIComponent(id)}`, { method: 'PUT', body, token, signal });
export const deleteEvento = (id, token, signal) => request(`/eventos/${encodeURIComponent(id)}`, { method: 'DELETE', token, signal });
export const createInscricao = (eventoId, token, signal) => request('/inscricoes', { method: 'POST', body: { eventoId }, token, signal });
export const getInscricao = (id, token, signal) => request(`/inscricoes/${encodeURIComponent(id)}`, { token, signal });
export const deleteInscricao = (id, token, signal) => request(`/inscricoes/${encodeURIComponent(id)}`, { method: 'DELETE', token, signal });
export const createUsuario = (body, token, signal) => request('/usuarios', { method: 'POST', body, token, signal });
export const updateUsuario = (id, body, token, signal) => request(`/usuarios/${encodeURIComponent(id)}`, { method: 'PUT', body, token, signal });
export const deleteUsuario = (id, token, signal) => request(`/usuarios/${encodeURIComponent(id)}`, { method: 'DELETE', token, signal });

export async function getEventWorkspace(token, signal) {
  const [eventos, categorias, inscricoes] = await Promise.all([
    getEventos(token, signal), getCategorias(token, signal), getInscricoes(token, signal),
  ]);
  return { eventos, categorias, inscricoes };
}

export async function getRegistrationWorkspace(token, signal) {
  const [inscricoes, eventos] = await Promise.all([getInscricoes(token, signal), getEventos(token, signal)]);
  return { inscricoes, eventos };
}
