import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, get, post, request, setJwt } from './http';

afterEach(()=>{vi.unstubAllGlobals();setJwt(null)});
describe('Cliente HTTP centralizado',()=>{
  it('envía solicitudes únicamente por /backend y agrega JWT',async()=>{
    const fetchSpy=vi.fn().mockResolvedValue(new Response(JSON.stringify({id:14}),{status:200,headers:{'Content-Type':'application/json'}}));
    vi.stubGlobal('fetch',fetchSpy);setJwt('token-solo-para-test');
    await expect(get<{id:number}>('/eventos/14')).resolves.toEqual({id:14});
    const [url,options]=fetchSpy.mock.calls[0];
    expect(url).toBe('/backend/eventos/14');
    expect(new Headers(options.headers).get('Authorization')).toBe('Bearer token-solo-para-test');
  });
  it('maneja respuestas 204 sin cuerpo',async()=>{
    vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response(null,{status:204})));
    await expect(request<void>('/prueba',{method:'DELETE'})).resolves.toBeNull();
  });
  it('muestra mensaje del backend y código de correlación',async()=>{
    vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response(JSON.stringify({mensaje:'El asiento está ocupado'}),{status:409,headers:{'X-Correlation-ID':'trace-001'}})));
    await expect(post('/reservas',{})).rejects.toMatchObject({status:409,message:'El asiento está ocupado',correlationId:'trace-001'});
  });
  it('diferencia error de red, sin convertirlo en una respuesta inventada',async()=>{
    vi.stubGlobal('fetch',vi.fn().mockRejectedValue(new Error('offline')));
    await expect(get('/eventos/publicados')).rejects.toBeInstanceOf(ApiError);
    await expect(get('/eventos/publicados')).rejects.toMatchObject({status:0});
  });
  it('notifica expiración al recibir 401 autenticado',async()=>{
    setJwt('test-token');const observer=vi.fn();window.addEventListener('ticketya:expired',observer);
    vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response(null,{status:401})));
    await expect(get('/usuarios/me')).rejects.toMatchObject({status:401});
    expect(observer).toHaveBeenCalledOnce();window.removeEventListener('ticketya:expired',observer);
  });
  it('rechaza rutas no relativas',async()=>{
    await expect(get('https://localhost:8085/auth/login')).rejects.toThrow('Ruta API no válida');
  });
});
