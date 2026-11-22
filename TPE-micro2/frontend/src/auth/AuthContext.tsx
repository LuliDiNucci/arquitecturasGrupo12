import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { authApi } from '../api/ticketya';
import { ApiError, setJwt } from '../api/http';
import type { LoginResponse, Role, User } from '../types/contracts';

const KEY = 'ticketya.session';
type Session = Pick<LoginResponse, 'accessToken' | 'expiresAt' | 'usuario'>;
function stored(): Session | null {
  try { const raw = sessionStorage.getItem(KEY); if (!raw) return null; const s = JSON.parse(raw) as Session;
    if (!s.accessToken || !s.expiresAt || Date.parse(s.expiresAt) <= Date.now()) { sessionStorage.removeItem(KEY); return null; }
    return s;
  } catch { sessionStorage.removeItem(KEY); return null; }
}
const Auth = createContext<{ user: User|null; login: (email: string, password: string) => Promise<void>; logout: () => void; has: (...roles: Role[]) => boolean; loading: boolean } | null>(null);

export function AuthProvider({children}: {children: ReactNode}) {
  const [session, setSession] = useState<Session|null>(() => stored());
  const [loading, setLoading] = useState(() => !!stored());
  setJwt(session?.accessToken || null);
  const logout = () => { sessionStorage.removeItem(KEY); setJwt(null); setSession(null); setLoading(false); };
  useEffect(() => {
    let active = true;
    if (!session) { setLoading(false); return; }
    authApi.me().then(usuario => { if (active) { const updated = { ...session, usuario }; setSession(updated); sessionStorage.setItem(KEY, JSON.stringify(updated)); } })
      .catch(error => { if (active && error instanceof ApiError && (error.status===401||error.status===403)) logout(); }).finally(() => { if (active) setLoading(false); });
    return () => {active = false;};
    // Revalidate only when a new token is established, not on every user update.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.accessToken]);
  useEffect(() => {
    const expire = () => logout();
    window.addEventListener('ticketya:expired', expire);
    const timer = window.setInterval(() => { if (session && Date.parse(session.expiresAt) <= Date.now()) logout(); }, 15000);
    return () => { window.removeEventListener('ticketya:expired', expire); clearInterval(timer); };
  }, [session]);
  const login = async (email: string, password: string) => {
    const result = await authApi.login(email, password);
    const next = { accessToken: result.accessToken, expiresAt: result.expiresAt, usuario: result.usuario };
    sessionStorage.setItem(KEY, JSON.stringify(next)); setJwt(next.accessToken); setSession(next); setLoading(false);
  };
  const has = (...roles: Role[]) => !!session?.usuario.roles.some(role => roles.includes(role));
  return <Auth.Provider value={{user: session?.usuario ?? null, login, logout, has, loading}}>{children}</Auth.Provider>;
}
export function useAuth() { const value = useContext(Auth); if (!value) throw new Error('AuthProvider requerido'); return value; }
export function Protected({roles, children}: {roles?: Role[]; children: ReactNode}) {
  const { user, has, loading } = useAuth(); const location = useLocation();
  if (loading) return <div className="center-state">Verificando sesión…</div>;
  if (!user) return <Navigate to="/ingresar" replace state={{from: location.pathname}}/>;
  if (roles && !has(...roles)) return <div className="page narrow"><div className="notice error">No tenés permisos para acceder a esta sección.</div></div>;
  return <>{children}</>;
}
