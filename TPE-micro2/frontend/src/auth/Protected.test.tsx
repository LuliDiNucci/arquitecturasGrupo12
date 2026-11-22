import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider, Protected } from './AuthContext';

afterEach(()=>{cleanup();sessionStorage.clear();vi.unstubAllGlobals()});
function session(roles:string[]){sessionStorage.setItem('ticketya.session',JSON.stringify({accessToken:'test-token',expiresAt:'2099-01-01T00:00:00Z',usuario:{id:7,nombre:'Test',apellido:'Usuario',email:'test@test.com',roles,activo:true}}));}
function subject(){return render(<MemoryRouter><AuthProvider><Protected roles={['ADMINISTRADOR']}><h1>Panel exclusivo</h1></Protected></AuthProvider></MemoryRouter>)}
describe('Permisos de navegación',()=>{
  it('deniega panel administrador a CLIENTE',async()=>{session(['CLIENTE']);vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response(JSON.stringify({id:7,nombre:'Test',apellido:'Usuario',email:'test@test.com',roles:['CLIENTE'],activo:true}),{status:200})));subject();expect(await screen.findByText(/No tenés permisos/)).toBeTruthy();expect(screen.queryByText('Panel exclusivo')).toBeNull()});
  it('habilita panel administrador solo al rol correspondiente',async()=>{session(['ADMINISTRADOR']);vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response(JSON.stringify({id:7,nombre:'Test',apellido:'Usuario',email:'test@test.com',roles:['ADMINISTRADOR'],activo:true}),{status:200})));subject();expect(await screen.findByText('Panel exclusivo')).toBeTruthy()});
});
