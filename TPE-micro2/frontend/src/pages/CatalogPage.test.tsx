import { afterEach, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { CatalogPage } from './CatalogPage';

afterEach(()=>{cleanup();vi.unstubAllGlobals()});
it('buscador usa los parámetros reales y muestra estado vacío sin inventar eventos',async()=>{
  const fetchSpy=vi.fn().mockResolvedValue(new Response(JSON.stringify([]),{status:200}));vi.stubGlobal('fetch',fetchSpy);
  const user=userEvent.setup();render(<MemoryRouter><CatalogPage/></MemoryRouter>);
  expect(await screen.findByText('No hay eventos con esos filtros')).toBeTruthy();
  await user.type(screen.getByLabelText('Buscar eventos por texto'),'Festival');
  await user.click(screen.getByRole('button',{name:/Buscar/}));
  expect(fetchSpy.mock.calls.some(c=>(c[0] as string).includes('/backend/eventos/buscar?q=Festival'))).toBe(true);
  expect(screen.queryByText('Evento de ejemplo')).toBeNull();
});
