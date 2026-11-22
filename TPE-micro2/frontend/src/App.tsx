import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { lazy, Suspense, useEffect } from 'react';
import { AuthProvider, Protected } from './auth/AuthContext';
import { Shell } from './layouts/Shell';
import { Empty, Loading } from './components/UI';

const CatalogPage = lazy(()=>import('./pages/CatalogPage').then(m=>({default:m.CatalogPage})));
const EventPage = lazy(()=>import('./pages/EventPage').then(m=>({default:m.EventPage})));
const CheckoutPage = lazy(()=>import('./pages/CheckoutPage').then(m=>({default:m.CheckoutPage})));
const TicketsPage = lazy(()=>import('./pages/TicketsPage').then(m=>({default:m.TicketsPage})));
const AuthPage = lazy(()=>import('./pages/AuthPages').then(m=>({default:m.AuthPage})));
const PreferencesPage = lazy(()=>import('./pages/PersonalPages').then(m=>({default:m.PreferencesPage})));
const RecommendationsPage = lazy(()=>import('./pages/PersonalPages').then(m=>({default:m.RecommendationsPage})));
const ProfilePage = lazy(()=>import('./pages/PersonalPages').then(m=>({default:m.ProfilePage})));
const OrganizerPage = lazy(()=>import('./pages/OrganizerPage').then(m=>({default:m.OrganizerPage})));
const AdminPage = lazy(()=>import('./pages/AdminPage').then(m=>({default:m.AdminPage})));
const AccessPage = lazy(()=>import('./pages/AccessPage').then(m=>({default:m.AccessPage})));

function ScrollReset(){const {pathname}=useLocation();useEffect(()=>{window.scrollTo(0,0)},[pathname]);return null;}
export default function App(){return <BrowserRouter><AuthProvider><ScrollReset/><Suspense fallback={<Loading/>}><Routes><Route element={<Shell/>}><Route index element={<CatalogPage/>}/><Route path="eventos/:id" element={<EventPage/>}/><Route path="ingresar" element={<AuthPage/>}/><Route path="registro" element={<AuthPage register/>}/><Route path="compra/:id" element={<Protected roles={['CLIENTE']}><CheckoutPage/></Protected>}/><Route path="mis-entradas" element={<Protected roles={['CLIENTE']}><TicketsPage/></Protected>}/><Route path="preferencias" element={<Protected roles={['CLIENTE']}><PreferencesPage/></Protected>}/><Route path="recomendaciones" element={<Protected roles={['CLIENTE']}><RecommendationsPage/></Protected>}/><Route path="perfil" element={<Protected><ProfilePage/></Protected>}/><Route path="organizador" element={<Protected roles={['ORGANIZADOR','ADMINISTRADOR']}><OrganizerPage/></Protected>}/><Route path="admin" element={<Protected roles={['ADMINISTRADOR']}><AdminPage/></Protected>}/><Route path="acceso" element={<Protected roles={['PERSONAL_ACCESO']}><AccessPage/></Protected>}/><Route path="404" element={<div className="page container"><Empty title="Esta página no existe" to="/" cta="Ir a inicio"/></div>}/><Route path="*" element={<Navigate to="/404" replace/>}/></Route></Routes></Suspense></AuthProvider></BrowserRouter>}
