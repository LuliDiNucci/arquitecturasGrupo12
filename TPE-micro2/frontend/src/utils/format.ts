export const money = (value: number | null | undefined) => value == null ? 'Precio a consultar' : new Intl.NumberFormat('es-AR', {style:'currency',currency:'ARS',maximumFractionDigits:2}).format(Number(value));
export const date = (value: string | null | undefined) => value ? new Intl.DateTimeFormat('es-AR', { day:'2-digit',month:'long',year:'numeric',hour:'2-digit',minute:'2-digit' }).format(new Date(value)) : 'Fecha no disponible';
export const dateShort = (value: string) => new Intl.DateTimeFormat('es-AR', {day:'2-digit',month:'short',year:'numeric'}).format(new Date(value));
export const remainingSeconds = (value: string, now = Date.now()) => Math.max(0, Math.floor((new Date(value).getTime() - now) / 1000));
export const timeLeft = (s: number) => `${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`;
export const pretty = (value: string) => value.replaceAll('_',' ').toLowerCase().replace(/^./, s=>s.toUpperCase());
export const integer = (value: string) => { const n = Number(value); return Number.isSafeInteger(n) && n > 0 ? n : null; };
