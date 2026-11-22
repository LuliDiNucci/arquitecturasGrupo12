import { del, get, params, patch, post, put } from './http';
import type { Availability, EventDetail, EventInput, EventReport, EventSector, EventSort, EventSummary, EventType, Location, LoginResponse, Organizer, Participant, Payment, Preferences, PreferencesInput, Purchase, QueueEntry, Recommendation, Reimbursement, Refund, Reservation, Role, Seat, Sector, Ticket, User } from '../types/contracts';
type Filter = { q?: string; tipo?: EventType | ''; ciudad?: string; organizadorId?: string; participanteId?: string; desde?: string; hasta?: string; ordenar?: EventSort; incluirNoPublicados?: boolean };
const id = (value: number) => encodeURIComponent(String(value));

export const authApi = {
  login: (email: string, password: string) => post<LoginResponse>('/auth/login', { email, password }),
  register: (data: { nombre: string; apellido: string; email: string; password: string }) => post<User>('/auth/register', data),
  me: () => get<User>('/usuarios/me'),
  user: (userId: number) => get<User>(`/usuarios/${id(userId)}`),
  roles: (userId: number, roles: Role[]) => patch<User>(`/usuarios/${id(userId)}/roles`, { roles }),
  state: (userId: number, activo: boolean) => patch<User>(`/usuarios/${id(userId)}/estado`, { activo })
};
export const eventsApi = {
  published: () => get<EventSummary[]>('/eventos/publicados'),
  search: (filter: Filter) => get<EventSummary[]>(`/eventos/buscar?${params(filter)}`),
  all: () => get<EventSummary[]>('/eventos'),
  detail: (eventId: number) => get<EventDetail>(`/eventos/${id(eventId)}`),
  sectors: (eventId: number) => get<EventSector[]>(`/eventos/${id(eventId)}/sectores`),
  create: (data: EventInput) => post<EventDetail>('/eventos', data),
  update: (eventId: number, data: EventInput) => put<EventDetail>(`/eventos/${id(eventId)}`, data),
  prices: (eventId: number, prices: { sectorId: number; precio: number }[]) => put<EventDetail>(`/eventos/${id(eventId)}/precios`, { precios: prices }),
  transition: (eventId: number, action: 'publicar'|'suspender'|'cancelar'|'agotar'|'reabrir'|'finalizar') => patch<EventDetail>(`/eventos/${id(eventId)}/${action}`),
  report: (eventId: number) => get<EventReport>(`/eventos/${id(eventId)}/reporte`),
  locations: () => get<Location[]>('/lugares'),
  location: (locationId: number) => get<Location>(`/lugares/${id(locationId)}`),
  createLocation: (data: Omit<Location, 'id'>) => post<Location>('/lugares', data),
  sectorsAt: (locationId: number) => get<Sector[]>(`/lugares/${id(locationId)}/sectores`),
  createSector: (locationId: number, data: Pick<Sector, 'nombre'|'numerado'|'capacidad'>) => post<Sector>(`/lugares/${id(locationId)}/sectores`, data),
  seats: (sectorId: number) => get<Seat[]>(`/sectores/${id(sectorId)}/asientos`),
  addSeats: (sectorId: number, asientos: { fila: string; numero: number }[]) => post<Seat[]>(`/sectores/${id(sectorId)}/asientos`, { asientos }),
  participants: () => get<Participant[]>('/participantes'),
  addParticipant: (data: Pick<Participant, 'nombre'|'tipo'>) => post<Participant>('/participantes', data),
  organizers: () => get<Organizer[]>('/organizadores'),
  addOrganizer: (data: Omit<Organizer, 'id'>) => post<Organizer>('/organizadores', data),
  linkOrganizer: (organizerId: number, userId: number) => patch<Organizer>(`/organizadores/${id(organizerId)}/usuario`, { usuarioId: userId })
};
export const checkoutApi = {
  availability: (eventId: number, sectorId: number) => get<Availability>(`/disponibilidad?${params({eventoId: eventId, sectorId})}`),
  reserve: (clienteId: number, eventoId: number, sectorId: number, asientoIds: number[], cantidad: number, claveIdempotencia: string) => post<Reservation>('/reservas', { clienteId, eventoId, claveIdempotencia, detalles: [{ sectorId, cantidad, asientoIds }] }),
  reservation: (reservationId: number) => get<Reservation>(`/reservas/${id(reservationId)}`),
  purchase: (clienteId: number, reservaId: number) => post<Purchase>('/compras', { clienteId, reservaId }),
  purchaseById: (purchaseId: number) => get<Purchase>(`/compras/${id(purchaseId)}`),
  pay: (purchaseId: number, medioPago: string, claveIdempotencia: string) => post<Payment>(`/compras/${id(purchaseId)}/pagar`, { medioPago, claveIdempotencia }),
  confirm: (purchaseId: number) => post<Purchase>(`/compras/${id(purchaseId)}/confirmar`),
  retryRelease: (purchaseId: number) => post<void>(`/compras/${id(purchaseId)}/reintentar-liberacion`)
};
export const ticketApi = {
  mine: (clienteId: number) => get<Ticket[]>(`/entradas/cliente/${id(clienteId)}`),
  detail: (ticketId: number, clienteId: number) => get<Ticket>(`/entradas/${id(ticketId)}`, { 'X-Cliente-Id': String(clienteId) }),
  refund: (entradaId: number, motivo: string) => post<Refund>('/devoluciones', { entradaId, motivo }),
  reimbursements: (purchaseId: number) => get<Reimbursement[]>(`/compras/${id(purchaseId)}/reintegros`),
  pending: () => get<Refund[]>('/devoluciones/pendientes'),
  resolve: (refundId: number, action: 'aprobar'|'rechazar') => patch<Refund>(`/devoluciones/${id(refundId)}/${action}`),
  validate: (codigoQr: string, eventoId: number) => post<Ticket>('/entradas/validar', { codigoQr, eventoId }),
  assign: (eventoId: number, usuarioId: number) => put<void>(`/entradas/accesos/eventos/${id(eventoId)}/personal/${id(usuarioId)}`, undefined),
  unassign: (eventoId: number, usuarioId: number) => del(`/entradas/accesos/eventos/${id(eventoId)}/personal/${id(usuarioId)}`)
};
export const personalizationApi = {
  preferences: (clienteId: number) => get<Preferences>(`/api/preferencias/${id(clienteId)}`),
  create: (clienteId: number, data: PreferencesInput) => post<Preferences>(`/api/preferencias/${id(clienteId)}`, data),
  update: (clienteId: number, data: PreferencesInput) => put<Preferences>(`/api/preferencias/${id(clienteId)}`, data),
  recommendations: (clienteId: number) => get<Recommendation[]>(`/api/recomendaciones/${id(clienteId)}`)
};
export const queueApi = {
  join: (eventoId: number, clienteId: number) => post<QueueEntry>(`/filas/${id(eventoId)}/ingresar`, { clienteId }),
  status: (eventoId: number, clienteId: number) => get<QueueEntry>(`/filas/${id(eventoId)}/clientes/${id(clienteId)}`),
  authorize: (eventoId: number, cantidad: number) => post<QueueEntry[]>(`/filas/${id(eventoId)}/habilitar`, { cantidad })
};
