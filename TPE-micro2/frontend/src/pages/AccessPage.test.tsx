import { afterEach, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AccessPage } from './AccessPage';

afterEach(()=>{cleanup();vi.unstubAllGlobals()});
it('nunca valida QR solo en el navegador: envía datos exactos a /entradas/validar',async()=>{
  const fetchSpy=vi.fn().mockResolvedValue(new Response(JSON.stringify({id:51,eventoId:4,estado:'UTILIZADA'}),{status:200}));
  vi.stubGlobal('fetch',fetchSpy);const user=userEvent.setup();render(<AccessPage/>);
  await user.type(screen.getByPlaceholderText('Identificador del evento'),'4');
  await user.type(screen.getByPlaceholderText('Pegá el código del QR'),'QR-de-test');
  expect(fetchSpy).not.toHaveBeenCalled();
  await user.click(screen.getByRole('button',{name:/Validar entrada/}));
  expect(await screen.findByText('Acceso permitido')).toBeTruthy();
  expect(fetchSpy).toHaveBeenCalledOnce();
  expect(fetchSpy.mock.calls[0][0]).toBe('/backend/entradas/validar');
  expect(fetchSpy.mock.calls[0][1].body).toBe(JSON.stringify({codigoQr:'QR-de-test',eventoId:4}));
});
