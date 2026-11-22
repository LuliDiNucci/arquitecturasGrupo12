import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarDays, MapPin, QrCode, RefreshCcw, Ticket as TicketIcon } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import { eventsApi, ticketApi } from '../api/ticketya';
import { Empty, Loading, Notice, ReloadError, SectionTitle, Status } from '../components/UI';
import { useAction, useRemote } from '../hooks/useRemote';
import type { EventDetail, Reimbursement, Ticket } from '../types/contracts';
import { date, money } from '../utils/format';

export function TicketsPage(){
  const {user}=useAuth();
  const tickets=useRemote(()=>ticketApi.mine(user!.id),[user?.id]);
  const [events,setEvents]=useState<Record<number,EventDetail>>({});
  const [selected,setSelected]=useState<Ticket|null>(null);
  const [motivo,setMotivo]=useState('');
  const [reimbursements,setReimbursements]=useState<Reimbursement[]|null>(null);
  const action=useAction();

  useEffect(()=>{
    if(!tickets.data)return;
    const ids=[...new Set(tickets.data.map(t=>t.eventoId))];let active=true;
    Promise.allSettled(ids.map(id=>eventsApi.detail(id))).then(results=>{
      if(!active)return;
      const map:Record<number,EventDetail>={};
      results.forEach((r,i)=>{if(r.status==='fulfilled')map[ids[i]]=r.value});
      setEvents(map);
    });
    return()=>{active=false};
  },[tickets.data]);

  const canReturn=(t:Ticket)=>t.estado==='EMITIDA' && !!events[t.eventoId]
    && (new Date(events[t.eventoId].fechaHora).getTime()-Date.now())>=72*60*60*1000;
  function open(t:Ticket|null){setSelected(t);setReimbursements(null);setMotivo('');action.setError('');}
  async function refund(ticket:Ticket){
    const result=await action.run(()=>ticketApi.refund(ticket.id,motivo),'Solicitud registrada. Un administrador deberá resolverla.');
    if(result){open(null);setMotivo('');void tickets.reload();}
  }
  async function loadReimbursements(ticket:Ticket){
    const result=await action.run(()=>ticketApi.reimbursements(ticket.compraId));
    if(result)setReimbursements(result);
  }
  return <div className="page"><div className="container">
    <SectionTitle eyebrow="TU CUENTA" title="Mis entradas" subtitle="Todos tus accesos, QR y solicitudes, directamente desde TicketYa."/>
    {tickets.loading?<Loading/>:tickets.error?<ReloadError message={tickets.error} reload={tickets.reload}/>:!tickets.data?.length?
      <Empty title="Todavía no tenés entradas" detail="Cuando confirmes una compra, vas a encontrarlas acá con su QR original." to="/" cta="Descubrir eventos"/>:
      <>
        <div className="ticket-list">{tickets.data.map(ticket=><div className="ticket-row" key={ticket.id}>
          <div className="ticket-stub"><TicketIcon size={26}/><span>#{ticket.id}</span></div>
          <div className="ticket-info">
            <span className="eyebrow">ENTRADA DIGITAL · COMPRA #{ticket.compraId}</span>
            <h3>{events[ticket.eventoId]?.nombre||`Evento #${ticket.eventoId}`}</h3>
            <p><CalendarDays size={15}/>{events[ticket.eventoId]?date(events[ticket.eventoId].fechaHora):'Detalle no disponible'}</p>
            <p><MapPin size={15}/>Sector #{ticket.sectorId} {ticket.asientoId?`· Asiento #${ticket.asientoId}`:'· Sin asiento numerado'}</p>
            <Status value={ticket.estado}/>
          </div>
          <div className="ticket-actions">
            <button className="btn btn-secondary" onClick={()=>open(selected?.id===ticket.id?null:ticket)}><QrCode size={18}/> {selected?.id===ticket.id?'Ocultar QR':'Ver entrada'}</button>
            {canReturn(ticket)&&<button className="btn btn-text" onClick={()=>open(ticket)}>Solicitar devolución</button>}
          </div>
        </div>)}</div>
        <Notice error={action.error} success={action.success}/>
        {selected&&<div className="ticket-modal-backdrop" onClick={()=>open(null)}>
          <div className="ticket-modal" onClick={e=>e.stopPropagation()} role="dialog" aria-modal="true" aria-label="Detalle de entrada">
            <button className="modal-close" onClick={()=>open(null)}>Cerrar ×</button>
            <span className="eyebrow">TICKETYA · ENTRADA #{selected.id}</span>
            <h2>{events[selected.eventoId]?.nombre||`Evento #${selected.eventoId}`}</h2>
            {selected.qrImagenBase64?
              <img className="qr-image" alt={`QR de la entrada ${selected.id}`} src={selected.qrImagenBase64.startsWith('data:')?selected.qrImagenBase64:`data:image/png;base64,${selected.qrImagenBase64}`}/>:
              <div className="notice">El backend no devolvió una imagen QR para esta entrada.</div>}
            <p><Status value={selected.estado}/> · Compra #{selected.compraId}</p>
            <p className="code-line">{selected.codigoQr}</p>
            {canReturn(selected)&&<div className="refund-form">
              <label>Motivo de devolución<textarea required minLength={3} maxLength={1000} rows={3} placeholder="Contanos por qué solicitás la devolución" value={motivo} onChange={e=>setMotivo(e.target.value)}/></label>
              <button disabled={action.busy||motivo.trim().length<3} className="btn btn-primary btn-wide" onClick={()=>refund(selected)}>{action.busy?'Enviando…':'Solicitar devolución'}</button>
              <small>Solo hasta 72 horas antes del evento. Sujeto a aprobación administrativa.</small>
            </div>}
            <button className="btn btn-secondary btn-wide" disabled={action.busy} onClick={()=>loadReimbursements(selected)}>{action.busy?'Consultando…':'Consultar reintegros de la compra'}</button>
            <Notice error={action.error}/>
            {reimbursements&&<div className="reimbursement-list">{!reimbursements.length?<p>No se registran reintegros para esta compra.</p>:reimbursements.map(r=><div key={r.id}><strong>Reintegro #{r.id} · {money(r.importe)}</strong><p>Entrada #{r.entradaId} · <Status value={r.estado}/></p></div>)}</div>}
          </div>
        </div>}
      </>}
    <p className="fineprint">Los estados y códigos QR se consultan al backend. <Link to="/">Buscar más eventos</Link></p>
    <button className="btn btn-text" onClick={()=>void tickets.reload()}><RefreshCcw size={15}/> Actualizar</button>
  </div></div>;
}
