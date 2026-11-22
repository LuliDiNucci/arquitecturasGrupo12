import { afterEach, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { AuthPage } from './AuthPages';

afterEach(()=>{cleanup();sessionStorage.clear();vi.unstubAllGlobals()});
it('registrar cliente envía solo campos habilitados y luego inicia sesión',async()=>{
  const mockUser={id:4,nombre:'Lucía',apellido:'Prueba',email:'lu@ticketya.test',roles:['CLIENTE'],activo:true};
  const fetchSpy=vi.fn().mockImplementation((url:string)=>{
    if(url==='/backend/auth/register')return Promise.resolve(new Response(JSON.stringify(mockUser),{status:201}));
    if(url==='/backend/auth/login')return Promise.resolve(new Response(JSON.stringify({accessToken:'test',expiresAt:'2099-01-01T00:00:00Z',usuario:mockUser}),{status:200}));
    return Promise.resolve(new Response(JSON.stringify(mockUser),{status:200}));
  });vi.stubGlobal('fetch',fetchSpy);
  const user=userEvent.setup();render(<MemoryRouter initialEntries={['/registro']}><AuthProvider><Routes><Route path="/registro" element={<AuthPage register/>}/><Route path="/" element={<div>Inicio correcto</div>}/></Routes></AuthProvider></MemoryRouter>);
  await user.type(screen.getByLabelText('Nombre'),'Lucía');
  await user.type(screen.getByLabelText('Apellido'),'Prueba');
  await user.type(screen.getByLabelText('Correo electrónico'),'lu@ticketya.test');
  await user.type(screen.getByLabelText('Contraseña'),'Contraseña-Test-2026');
  await user.click(screen.getByRole('button',{name:/Crear mi cuenta/}));
  expect(await screen.findByText('Inicio correcto')).toBeTruthy();
  const call=fetchSpy.mock.calls.find(c=>c[0]==='/backend/auth/register');
  expect(call).toBeTruthy();
  expect(Object.keys(JSON.parse(call![1].body)).sort()).toEqual(['apellido','email','nombre','password']);
  expect(fetchSpy.mock.calls.some(c=>c[0]==='/backend/auth/login')).toBe(true);
});
