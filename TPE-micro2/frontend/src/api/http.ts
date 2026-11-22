export class ApiError extends Error {
  constructor(public status: number, message: string, public correlationId?: string) { super(message); this.name = 'ApiError'; }
}

let jwt: string | null = null;
export function setJwt(token: string | null) { jwt = token; }
const httpMessages: Record<number, string> = {
  400: 'Revisá los datos ingresados.', 401: 'Tu sesión venció. Volvé a iniciar sesión.',
  403: 'No tenés permisos para realizar esta acción.', 404: 'El recurso solicitado no existe.',
  409: 'La operación entra en conflicto con el estado actual.',
  429: 'Demasiados intentos. Esperá unos minutos.', 503: 'El servicio no está disponible en este momento.'
};

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  if (!path.startsWith('/') || path.startsWith('//')) throw new Error('Ruta API no válida');
  const headers = new Headers(options.headers);
  if (options.body !== undefined && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (jwt) headers.set('Authorization', `Bearer ${jwt}`);
  headers.set('Accept', 'application/json');
  let response: Response;
  try { response = await fetch(`/backend${path}`, { ...options, headers, cache: 'no-store' }); }
  catch { throw new ApiError(0, 'No se pudo conectar con TicketYa. Revisá que el Gateway esté encendido.'); }
  const correlationId = response.headers.get('X-Correlation-ID') || response.headers.get('X-Request-ID') || undefined;
  const raw = response.status === 204 ? '' : await response.text();
  let parsed: unknown = null;
  if (raw) { try { parsed = JSON.parse(raw); } catch { parsed = raw; } }
  if (!response.ok) {
    if (response.status === 401 && jwt) window.dispatchEvent(new Event('ticketya:expired'));
    const body = parsed && typeof parsed === 'object' ? parsed as Record<string, unknown> : null;
    const msg = body && (body.mensaje || body.message || body.detail || body.error);
    throw new ApiError(response.status, typeof msg === 'string' && msg !== 'Bad Request' && msg !== 'Forbidden' ? msg : httpMessages[response.status] || `Error HTTP ${response.status}`, correlationId);
  }
  return parsed as T;
}

export function get<T>(path: string, headers?: HeadersInit) { return request<T>(path, { headers }); }
export function post<T>(path: string, data?: unknown) { return request<T>(path, { method: 'POST', ...(data === undefined ? {} : { body: JSON.stringify(data) }) }); }
export function put<T>(path: string, data: unknown) { return request<T>(path, { method: 'PUT', body: JSON.stringify(data) }); }
export function patch<T>(path: string, data?: unknown) { return request<T>(path, { method: 'PATCH', ...(data === undefined ? {} : { body: JSON.stringify(data) }) }); }
export function del(path: string) { return request<void>(path, { method: 'DELETE' }); }
export function params(values: Record<string, string | number | boolean | undefined | null>) {
  const p = new URLSearchParams(); Object.entries(values).forEach(([k,v]) => { if (v !== undefined && v !== null && v !== '') p.set(k, String(v)); });
  return p.toString();
}
export function messageOf(error: unknown) {
  return error instanceof ApiError ? `${error.message}${error.correlationId ? ` (referencia ${error.correlationId})` : ''}` : error instanceof Error ? error.message : 'Ocurrió un error inesperado.';
}
