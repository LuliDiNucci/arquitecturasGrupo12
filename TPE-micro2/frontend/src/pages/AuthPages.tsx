import { useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, LockKeyhole, ShieldCheck } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import { authApi } from '../api/ticketya';
import { Notice } from '../components/UI';
import { useAction } from '../hooks/useRemote';

export function AuthPage({register=false}: {register?:boolean}) {
  const {user,login} = useAuth(); const nav = useNavigate(); const location = useLocation();
  const [form,setForm] = useState({nombre:'',apellido:'',email:'',password:''}); const action = useAction();
  if (user) return <Navigate to="/" replace/>;
  async function submit(e: FormEvent) { e.preventDefault();
    const result = await action.run(async()=>{
      if (register) await authApi.register(form);
      await login(form.email,form.password);
      return true;
    });
    if (result) nav(typeof location.state?.from === 'string' ? location.state.from : '/', {replace:true});
  }
  return <div className="auth-page"><div className="auth-aside"><div className="auth-art"><span>TY.</span></div><p className="eyebrow light">DONDE EMPIEZAN LAS HISTORIAS</p><h1>Tu próxima gran noche empieza acá.</h1><p>Recitales, teatro, deportes y experiencias que vas a recordar siempre.</p></div><div className="auth-panel"><form className="auth-form" onSubmit={submit}><div className="auth-icon"><LockKeyhole size={24}/></div><p className="eyebrow">BIENVENIDO A TICKETYA</p><h2>{register?'Creá tu cuenta':'Qué bueno verte de nuevo'}</h2><p>{register?'Registrate para reservar y recibir tus entradas.':'Ingresá para seguir disfrutando de tus eventos.'}</p><Notice error={action.error}/>{register && <div className="form-grid"><label>Nombre<input required maxLength={100} value={form.nombre} onChange={e=>setForm({...form,nombre:e.target.value})} autoComplete="given-name"/></label><label>Apellido<input required maxLength={100} value={form.apellido} onChange={e=>setForm({...form,apellido:e.target.value})} autoComplete="family-name"/></label></div>}<label>Correo electrónico<input type="email" required maxLength={254} value={form.email} onChange={e=>setForm({...form,email:e.target.value})} autoComplete="email" placeholder="tu@email.com"/></label><label>Contraseña<input type="password" required minLength={register?12:1} maxLength={72} value={form.password} onChange={e=>setForm({...form,password:e.target.value})} autoComplete={register?'new-password':'current-password'} placeholder={register?'Mínimo 12 caracteres':'Tu contraseña'}/></label>{register && <p className="fineprint"><ShieldCheck size={16}/> Solo se crea una cuenta CLIENTE. Los otros roles son asignados por administración.</p>}<button type="submit" disabled={action.busy} className="btn btn-primary btn-wide">{action.busy?'Un momento…':register?'Crear mi cuenta':'Iniciar sesión'}<ArrowRight size={18}/></button><p className="switch-auth">{register?'¿Ya tenés cuenta?':'¿Todavía no tenés cuenta?'} <Link to={register?'/ingresar':'/registro'}>{register?'Ingresá':'Registrate gratis'}</Link></p></form></div></div>;
}
