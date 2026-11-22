import { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, ArrowRight, CheckCircle2, Clock3, CreditCard, ShieldCheck, Ticket } from 'lucide-react';
import { checkoutApi } from '../api/ticketya';
import { useAuth } from '../auth/AuthContext';
import { Loading, Notice, ReloadError, Status } from '../components/UI';
import { useAction, useRemote } from '../hooks/useRemote';
import type { Purchase } from '../types/contracts';
import { money, remainingSeconds, timeLeft } from '../utils/format';

const methods = [{id:'TARJETA',label:'Tarjeta'},{id:'TRANSFERENCIA',label:'Transferencia'},{id:'BILLETERA',label:'Billetera virtual'},{id:'TARJETA_RECHAZADA',label:'Tarjeta rechazada (prueba)'}];
export function CheckoutPage() {
  const {id} = useParams();const reservationId=Number(id);const {user}=useAuth();
  const reservation = useRemote(()=>checkoutApi.reservation(reservationId),[reservationId]);
  const [purchase,setPurchase] = useState<Purchase|null>(null);
  const [method,setMethod] = useState('TARJETA');const [now,setNow] = useState(Date.now());
  const action = useAction();const lock=useRef(false);
  useEffect(()=>{const interval=setInterval(()=>setNow(Date.now()),1000);return()=>clearInterval(interval)},[]);
  useEffect(()=>{if(!user || !Number.isSafeInteger(reservationId)||reservationId<=0)return;
    const stored=sessionStorage.getItem(`ticketya.purchase.${user.id}.${reservationId}`);
    if(!stored)return;let mounted=true;checkoutApi.purchaseById(Number(stored)).then(p=>{if(mounted)setPurchase(p);}).catch(()=>{});
    return()=>{mounted=false};
  },[user?.id,reservationId]);
  const seconds=reservation.data?.fechaVencimiento?remainingSeconds(reservation.data.fechaVencimiento,now):0;
  const expired=seconds===0||reservation.data?.estado!=='ACTIVA';
  async function proceed(){if(!user||lock.current)return;lock.current=true;
    const next=await action.run(async()=>{
      const fresh=await checkoutApi.reservation(reservationId);
      if(fresh.estado!=='ACTIVA'||remainingSeconds(fresh.fechaVencimiento)===0)throw new Error('La reserva está vencida o ya no se encuentra activa.');
      const value=await checkoutApi.purchase(user.id,reservationId);
      sessionStorage.setItem(`ticketya.purchase.${user.id}.${reservationId}`,String(value.id));
      return value;
    },'Compra iniciada. Elegí un medio de pago.');
    if(next)setPurchase(next);lock.current=false;
  }
  async function pay(){if(!user||!purchase||lock.current)return;lock.current=true;
    const result=await action.run(async()=>{
      const current=await checkoutApi.purchaseById(purchase.id);
      if(current.estado==='CONFIRMADA')return current;
      if(current.estado==='CONFIRMACION_PENDIENTE')return checkoutApi.confirm(purchase.id);
      if(current.estado!=='PENDIENTE')throw new Error(`La compra está en estado ${current.estado}.`);
      const fresh=await checkoutApi.reservation(reservationId);
      if(fresh.estado!=='ACTIVA'||remainingSeconds(fresh.fechaVencimiento)===0)throw new Error('La reserva ya no está activa o venció.');
      const keyName=`ticketya.payment-key.${user.id}.${purchase.id}`;
      const methodKey=`ticketya.payment-method.${user.id}.${purchase.id}`;
      const existingMethod=sessionStorage.getItem(methodKey);
      if(existingMethod&&existingMethod!==method)throw new Error('Ya se intentó pagar con otro medio. Para recuperar el resultado, usá el mismo medio seleccionado.');
      let key=sessionStorage.getItem(keyName);if(!key){key=crypto.randomUUID();sessionStorage.setItem(keyName,key);sessionStorage.setItem(methodKey,method);}
      const payment=await checkoutApi.pay(purchase.id,method,key);
      if(payment.estado!=='APROBADO')throw new Error(`El pago fue ${payment.estado.toLowerCase()}. Consultá el estado antes de volver a intentar.`);
      return checkoutApi.confirm(purchase.id);
    },'¡Compra confirmada! Tus entradas se emitieron correctamente.');
    if(result)setPurchase(result);else {try{const fresh=await checkoutApi.purchaseById(purchase.id);setPurchase(fresh);}catch{/* Conservar el último estado confirmado. */}}
    lock.current=false;
  }
  async function refresh(){if(!purchase)return;const next=await action.run(()=>checkoutApi.purchaseById(purchase.id));if(next)setPurchase(next);}
  async function retryRelease(){if(!purchase)return;const result=await action.run(()=>checkoutApi.retryRelease(purchase.id),'Se completó la liberación pendiente.');if(result!==null)void refresh();}
  if(!Number.isSafeInteger(reservationId)||reservationId<=0)return <div className="page"><Notice error="Reserva inválida."/></div>;
  if(reservation.loading)return <div className="page"><Loading/></div>;
  if(reservation.error||!reservation.data)return <div className="page"><ReloadError message={reservation.error||'Reserva inexistente'} reload={reservation.reload}/></div>;
  return <div className="page checkout-page"><div className="container narrow"><Link className="backlink" to={`/eventos/${reservation.data.eventoId}`}><ArrowLeft size={16}/> Volver al evento</Link><div className="checkout-header"><span className="eyebrow">COMPRA SEGURA</span><h1>Ya casi es tuyo.</h1><p>Revisá tu reserva y completá el pago simulado de TicketYa.</p></div>{purchase?.estado==='CONFIRMADA'?<div className="complete-card"><div className="complete-icon"><CheckCircle2 size={37}/></div><span className="eyebrow">COMPRA CONFIRMADA</span><h2>¡Nos vemos en el evento!</h2><p>El backend confirmó la reserva y emitió las entradas.</p><p>Compra #{purchase.id} · <Status value={purchase.estado}/></p><Link to="/mis-entradas" className="btn btn-primary">Ver mis entradas <ArrowRight size={18}/></Link></div>:<div className="checkout-content"><div className="checkout-card"><div className="checkout-card-head"><Ticket size={22}/><div><h2>Tu reserva #{reservationId}</h2><span>Evento #{reservation.data.eventoId}</span></div><Status value={reservation.data.estado}/></div><div className={`timer-card ${expired?'timer-expired':''}`}><Clock3 size={22}/><div><b>{expired?'Reserva vencida o no activa':`Tiempo restante: ${timeLeft(seconds)}`}</b><p>La duración depende de la fecha de vencimiento del servidor.</p></div></div><div className="checkout-items">{reservation.data.detalles.map(d=><div className="checkout-item" key={d.id}><span>Sector #{d.sectorId} · {d.cantidad} {d.cantidad===1?'entrada':'entradas'}</span><small>{d.asientoIds?.length?`Asientos: ${d.asientoIds.join(', ')}`:'Sector sin numerar'}</small></div>)}</div>{purchase?<><div className="checkout-total"><span>Total de la compra</span><strong>{money(purchase.total)}</strong></div><div className="purchase-state">Compra #{purchase.id} · <Status value={purchase.estado}/></div></>:<button disabled={action.busy||expired} className="btn btn-primary btn-wide" onClick={proceed}>{action.busy?'Iniciando…':'Iniciar compra'} <ArrowRight size={17}/></button>}</div>{purchase&&purchase.estado!=='CONFIRMADA'&&<div className="checkout-card"><h2>Medio de pago</h2><p className="subtle">Pasarela simulada del trabajo práctico. No se cobra dinero real.</p>{purchase.estado==='PENDIENTE' && <div className="payment-methods">{methods.map(m=><label className={method===m.id?'payment-option active':'payment-option'} key={m.id}><input type="radio" checked={method===m.id} onChange={()=>setMethod(m.id)} name="medio"/><CreditCard size={20}/><span>{m.label}</span></label>)}</div>}<Notice error={action.error} success={action.success}/><button className="btn btn-primary btn-wide" disabled={action.busy||(purchase.estado==='PENDIENTE'&&expired)||!['PENDIENTE','CONFIRMACION_PENDIENTE'].includes(purchase.estado)} onClick={pay}>{action.busy?'Procesando…':purchase.estado==='CONFIRMACION_PENDIENTE'?'Reintentar confirmación':'Pagar y confirmar'} <ShieldCheck size={18}/></button>{purchase.estado==='LIBERACION_PENDIENTE' && <button className="btn btn-secondary btn-wide" disabled={action.busy} onClick={retryRelease}>Reintentar liberación</button>}<button className="btn btn-text btn-wide" disabled={action.busy} onClick={refresh}>Consultar estado real de la compra</button></div>}<Notice error={!purchase?action.error:''} success={!purchase?action.success:''}/></div>}</div></div>;
}
