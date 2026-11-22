// Contratos revisados contra los DTO Java y controladores incluidos en TPE-micro2.
export type Role = 'CLIENTE' | 'ORGANIZADOR' | 'ADMINISTRADOR' | 'PERSONAL_ACCESO';
export type EventType = 'RECITAL' | 'FESTIVAL' | 'TEATRO' | 'DEPORTE' | 'CONFERENCIA' | 'OTRO';
export type EventSort = 'FECHA_ASC' | 'FECHA_DESC' | 'PRECIO_ASC' | 'PRECIO_DESC' | 'RELEVANCIA';
export const TYPES: EventType[] = ['RECITAL', 'FESTIVAL', 'TEATRO', 'DEPORTE', 'CONFERENCIA', 'OTRO'];
export interface User { id: number; nombre: string; apellido: string; email: string; roles: Role[]; activo: boolean; fechaCreacion: string }
export interface LoginResponse { accessToken: string; tokenType: string; expiresIn: number; expiresAt: string; usuario: User }
export interface EventSummary { id: number; nombre: string; tipo: EventType; estado: string; fechaHora: string; ciudad: string; lugar: string; organizador: string; precioDesde: number | null; altaDemanda: boolean }
export interface Location { id: number; nombre: string; direccion: string; ciudad: string; latitud: number; longitud: number }
export interface Seat { id: number; fila: string; numero: number; sectorId: number }
export interface Sector { id: number; lugarId: number; nombre: string; numerado: boolean; capacidad: number; asientosCargados: number }
export interface EventSector { sectorId: number; nombre: string; numerado: boolean; capacidad: number; precio: number; asientos: Seat[] }
export interface Organizer { id: number; nombre: string; email: string }
export interface Participant { id: number; nombre: string; tipo: string }
export interface EventDetail { id: number; nombre: string; descripcion: string; tipo: EventType; estado: string; organizador: Organizer; participantes: Participant[]; fechaHora: string; lugar: Location; altaDemanda: boolean; sectores: EventSector[]; version: number }
export interface EventInput { nombre: string; descripcion: string; tipo: EventType; organizadorId: number; lugarId: number; fechaHora: string; participantesIds: number[]; altaDemanda: boolean }
export interface Availability { eventoId: number; sectorId: number; lugaresDisponibles: number; asientosDisponibles: number[]; asientosOcupados: number[] }
export interface ReservationDetail { id: number; sectorId: number; cantidad: number; cantidadLiberada: number; asientoIds: number[] }
export interface Reservation { id: number; clienteId: number; eventoId: number; fechaCreacion: string; fechaVencimiento: string; estado: string; detalles: ReservationDetail[] }
export interface PurchaseItem { id: number; asientoId: number | null; entradaId: number | null }
export interface PurchaseDetail { id: number; eventoId: number; sectorId: number; cantidad: number; precioUnitario: number; subtotal: number; items: PurchaseItem[] }
export interface Purchase { id: number; clienteId: number; reservaId: number; eventoId: number; fechaCompra: string; total: number; estado: string; detalles: PurchaseDetail[] }
export interface Payment { id: number; compraId: number; importe: number; medioPago: string; estado: string; fechaSolicitud: string; fechaPago: string | null; referenciaExterna: string }
export interface Ticket { id: number; compraId: number; reservaId: number; detalleReservaId: number; indiceLugar: number; clienteId: number; eventoId: number; sectorId: number; asientoId: number | null; codigoQr: string; estado: string; fechaEmision: string; fechaUso: string | null; qrImagenBase64: string }
export interface Refund { id: number; entradaId: number; fechaSolicitud: string; motivo: string; estado: string; fechaResolucion: string | null; administradorId: number | null }
export interface Reimbursement { id: number; pagoId: number; compraId: number; entradaId: number; importe: number; estado: string; motivo: string; fechaSolicitud: string; fechaReintegro: string | null; referenciaExterna: string }
export interface Preferences { clienteId: number; tiposEventos: string[]; artistasFavoritos: string[]; ciudad: string }
export type PreferencesInput = Pick<Preferences, 'tiposEventos' | 'artistasFavoritos' | 'ciudad'>;
export interface Recommendation { id: number; nombre: string; tipo: string; artista: string; ciudad: string; fechaHora: string; precio: number }
export interface QueueEntry { id: number; eventoId: number; clienteId: number; fechaIngreso: string; estado: 'EN_ESPERA' | 'HABILITADO' | 'VENCIDO' | 'FINALIZADO'; posicion: number | null; fechaHabilitacion: string | null; fechaVencimiento: string | null }
export interface ReportSector { sectorId: number; nombreSector: string; numerado: boolean; capacidad: number; entradasVendidas: number; entradasDevueltas: number; entradasVigentes: number; lugaresOcupados: number; porcentajeOcupacion: number; importeBruto: number; importeReintegrado: number; totalRecaudado: number }
export interface EventReport { eventoId: number; nombreEvento: string; capacidadTotal: number; lugaresOcupados: number; porcentajeOcupacion: number; importePagado: number; importeReintegrado: number; recaudacionNeta: number; sectores: ReportSector[] }
