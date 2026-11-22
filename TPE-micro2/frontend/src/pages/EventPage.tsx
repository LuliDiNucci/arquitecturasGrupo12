import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, ArrowRight, CalendarDays, Clock3, MapPin, RefreshCcw, ShieldCheck, Ticket } from 'lucide-react';
import { checkoutApi, eventsApi, queueApi } from '../api/ticketya';
import { useAuth } from '../auth/AuthContext';
import { Empty, EventArtwork, Loading, Notice, ReloadError, Status } from '../components/UI';
import { useAction, useRemote } from '../hooks/useRemote';
import type { Availability, QueueEntry } from '../types/contracts';
import { date, money, pretty, remainingSeconds, timeLeft } from '../utils/format';

export function EventPage() {
  const {id} = useParams(); const eventId = Number(id); const nav = useNavigate();
  const {user,has} = useAuth(); const [sectorId,setSectorId] = useState<number|null>(null);
  const [seatIds,setSeatIds] = useState<number[]>([]); const [quantity,setQuantity] = useState(1);
  const eventQuery = useRemote(()=>eventsApi.detail(eventId),[eventId]);
  const sectorsQuery = useRemote(()=>eventsApi.sectors(eventId),[eventId]);
  const availability = useRemote<Availability>(()=>sectorId ? checkoutApi.availability(eventId,sectorId):Promise.resolve({eventoId:eventId,sectorId:0,lugaresDisponibles:0,asientosDisponibles:[],asientosOcupados:[]}),[eventId,sectorId]);
  const [queue,setQueue] = useState<QueueEntry|null>(null); const [queueError,setQueueError] = useState('');
  const action = useAction(); const join = useAction(); const processing = useRef(false);
  const event = eventQuery.data; const sectors = sectorsQuery.data || []; const selected = sectors.find(s=>s.sectorId===sectorId);
  useEffect(()=>{if (sectors.length && (sectorId===null || !sectors.some(s=>s.sectorId===sectorId))) setSectorId(sectors[0].sectorId);},[sectorsQuery.data,sectorId]);
  useEffect(()=>{setSeatIds([]);setQuantity(1)},[sectorId]);
  useEffect(()=>{ if (!event?.altaDemanda || !user || !has('CLIENTE')) return;
    let mounted=true; const refresh=()=>queueApi.status(eventId,user.id).then(q=>{if(mounted){setQueue(q);setQueueError('');}}).catch(e=>{if(mounted && e.status!==404) setQueueError(e.message);});
    void refresh(); const timer=setInterval(refresh,7000); return ()=>{mounted=false;clearInterval(timer)};
  },[event?.altaDemanda,eventId,user?.id]);
  async function joinQueue(){if(!user) return;const result=await join.run(()=>queueApi.join(eventId,user.id),'Ingresaste a la fila virtual.');if(result)setQueue(result);}
  const isQueueReady = !event?.altaDemanda || (queue?.estado==='HABILITADO' && !!queue.fechaVencimiento && remainingSeconds(queue.fechaVencimiento)>0);
  const canBuy = event?.estado==='PUBLICADO' && has('CLIENTE') && isQueueReady;
  const selectedQty = selected?.numerado?seatIds.length:quantity;
  async function reserve(){if(!user||!selected||!canBuy||processing.current)return;
    processing.current=true;
    await action.run(async()=>{
      const fresh=await checkoutApi.availability(eventId,selected.sectorId);
      if(selected.numerado && seatIds.some(s=>!fresh.asientosDisponibles.includes(s))) throw new Error('Uno de los asientos ya no está disponible. Actualizá la selección.');
      if(selectedQty<1 || selectedQty>fresh.lugaresDisponibles)throw new Error('La cantidad ya no está disponible. Actualizá el sector.');
      const keyName=`ticketya.reserve.${user.id}.${eventId}.${selected.sectorId}.${[...seatIds].sort().join('-')}.${selectedQty}`;
      let key=sessionStorage.getItem(keyName);if(!key){key=crypto.randomUUID();sessionStorage.setItem(keyName,key);}
      const reservation=await checkoutApi.reserve(user.id,eventId,selected.sectorId,seatIds,selectedQty,key);
      sessionStorage.setItem(`ticketya.checkout.${user.id}`,JSON.stringify({reservationId:reservation.id,eventId}));
      sessionStorage.removeItem(keyName);
      nav(`/compra/${reservation.id}`);
    });
    processing.current=false;
    if(action.error)void availability.reload();
  }
  if(!Number.isSafeInteger(eventId)||eventId<=0)return <div className="page"><Empty title="Evento inválido" to="/"/></div>;
  if(eventQuery.loading||sectorsQuery.loading)return <div className="page"><Loading/></div>;
  if(eventQuery.error||sectorsQuery.error||!event)return <div className="page"><ReloadError message={eventQuery.error||sectorsQuery.error||'Evento no encontrado'} reload={eventQuery.reload}/></div>;
  return <div className="event-page"><div className="event-banner"><EventArtwork type={event.tipo} big/><div className="event-banner-overlay"/></div><div className="container"><div className="event-breadcrumb"><Link to="/"><ArrowLeft size={16}/> Todos los eventos</Link><span>/ {pretty(event.tipo)}</span></div><div className="detail-grid"><div className="event-information"><div className="detail-badges"><span className="tag">{pretty(event.tipo)}</span>{event.altaDemanda && <span className="tag hot">Fila virtual</span>}<Status value={event.estado}/></div><h1>{event.nombre}</h1><p className="event-description">{event.descripcion}</p><div className="info-list"><div><span className="info-icon"><CalendarDays/></span><div><small>FECHA Y HORA</small><b>{date(event.fechaHora)}</b></div></div><div><span className="info-icon"><MapPin/></span><div><small>UBICACIÓN</small><b>{event.lugar.nombre}, {event.lugar.ciudad}</b><span>{event.lugar.direccion}</span></div></div><div><span className="info-icon"><Ticket/></span><div><small>ORGANIZA</small><b>{event.organizador.nombre}</b><span>{event.participantes.map(p=>p.nombre).join(' · ')}</span></div></div></div><div className="trust-note"><ShieldCheck size={20}/><div><strong>Compra segura a través de TicketYa</strong><p>El sistema verifica la disponibilidad al reservar y valida cada entrada con su código QR.</p></div></div></div><aside className="booking-panel"><div className="booking-title"><span className="eyebrow">RESERVÁ TU LUGAR</span><h2>Elegí tu entrada</h2></div>{event.estado!=='PUBLICADO' && <Notice error="Este evento no está habilitado para nuevas reservas."/>}{event.altaDemanda && <section className="queue-box"><div className="queue-box-top"><Clock3 size={20}/><strong>Evento de alta demanda</strong></div><p>Para reservar necesitás un turno vigente en la fila virtual.</p>{!user?<Link className="btn btn-secondary btn-wide" to="/ingresar" state={{from:`/eventos/${eventId}`}}>Ingresar para unirte</Link>:queue?<><p>Tu estado: <Status value={queue.estado}/>{queue.posicion!==null && ` · Posición ${queue.posicion}`}</p>{queue.fechaVencimiento && queue.estado==='HABILITADO'&&<QueueCountdown expires={queue.fechaVencimiento}/>}</>:<button onClick={joinQueue} disabled={join.busy} className="btn btn-secondary btn-wide">{join.busy?'Ingresando…':'Entrar a la fila'}</button>}<Notice error={join.error||queueError} success={join.success}/></section>}<label className="field-label">Sector<select value={sectorId||''} onChange={e=>setSectorId(Number(e.target.value))}>{sectors.map(s=><option value={s.sectorId} key={s.sectorId}>{s.nombre} · {money(s.precio)}</option>)}</select></label>{!sectors.length?<Empty title="Sin sectores configurados" detail="El organizador todavía no habilitó sectores para este evento."/>:availability.loading?<Loading label="Consultando disponibilidad…"/>:availability.error?<ReloadError message={availability.error} reload={availability.reload}/>:<><div className="availability-line"><span><span className="live-dot"/> Disponibilidad actual</span><b>{availability.data?.lugaresDisponibles??0} lugares</b><button className="icon-button" onClick={()=>void availability.reload()} title="Actualizar disponibilidad"><RefreshCcw size={16}/></button></div>{selected?.numerado?<><p className="subtle">Elegí tus asientos (máximo 4 por cliente y evento).</p><div className="seat-legend"><span><i className="seat-demo available"/> Libre</span><span><i className="seat-demo taken"/> Ocupado</span><span><i className="seat-demo picked"/> Seleccionado</span></div><div className="seat-grid">{(selected.asientos||[]).map(seat=>{const available=availability.data?.asientosDisponibles.includes(seat.id);const active=seatIds.includes(seat.id);return <button key={seat.id} title={`Fila ${seat.fila}, asiento ${seat.numero}`} aria-pressed={active} disabled={!available&&!active} className={`seat ${active?'picked':!available?'taken':''}`} onClick={()=>setSeatIds(prev=>active?prev.filter(v=>v!==seat.id):prev.length<4?[...prev,seat.id]:prev)}>{seat.fila}{seat.numero}</button>})}</div></>:<label className="field-label">Cantidad de entradas<select value={quantity} onChange={e=>setQuantity(Number(e.target.value))}>{Array.from({length:Math.min(4,availability.data?.lugaresDisponibles||0)},(_,i)=><option key={i+1} value={i+1}>{i+1} {i===0?'entrada':'entradas'}</option>)}</select></label>}<div className="booking-total"><span>Total estimado</span><strong>{money((selected?.precio||0)*selectedQty)}</strong></div><p className="fineprint">El importe definitivo se obtiene del servidor al iniciar la compra.</p><Notice error={action.error}/>{!user?<Link to="/ingresar" state={{from:`/eventos/${eventId}`}} className="btn btn-primary btn-wide">Ingresar para reservar <ArrowRight size={18}/></Link>:!has('CLIENTE')?<div className="notice">Se requiere el rol CLIENTE para reservar.</div>:<button onClick={reserve} disabled={action.busy||!canBuy||selectedQty===0||selectedQty>4||selectedQty>(availability.data?.lugaresDisponibles??0)} className="btn btn-primary btn-wide">{action.busy?'Reservando…':'Reservar entradas'} <ArrowRight size={18}/></button>}</>}</aside></div></div></div>;
}
function QueueCountdown({expires}:{expires:string}){const [now,setNow]=useState(Date.now());useEffect(()=>{const t=setInterval(()=>setNow(Date.now()),1000);return()=>clearInterval(t)},[]);return <small>Turno disponible: {timeLeft(remainingSeconds(expires,now))}</small>}
