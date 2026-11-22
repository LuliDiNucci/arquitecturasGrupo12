import { afterEach, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import App from './App';

afterEach(()=>{cleanup();sessionStorage.clear();vi.unstubAllGlobals();window.history.replaceState({},'', '/')});
it('abre el catálogo real en /',async()=>{
  vi.stubGlobal('scrollTo',vi.fn());vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response('[]',{status:200})));
  window.history.replaceState({},'','/');render(<App/>);
  expect(await screen.findByRole('heading',{name:/Eventos para disfrutar/})).toBeTruthy();
});
it('redirige rutas privadas al login sin sesión',async()=>{
  vi.stubGlobal('scrollTo',vi.fn());window.history.replaceState({},'','/mis-entradas');render(<App/>);
  expect(await screen.findByRole('heading',{name:/Qué bueno verte de nuevo/})).toBeTruthy();
});
it('muestra una ruta de error para direcciones inexistentes',async()=>{
  vi.stubGlobal('scrollTo',vi.fn());window.history.replaceState({},'','/ruta-que-no-existe');render(<App/>);
  expect(await screen.findByRole('heading',{name:/Esta página no existe/})).toBeTruthy();
});
