import { describe,expect,it } from 'vitest';
import { integer, remainingSeconds, timeLeft } from './format';
describe('Reglas de presentación sin lógica duplicada',()=>{
  it('muestra el vencimiento desde la fecha enviada por Reservas',()=>{const at=new Date('2026-11-22T15:00:00').getTime();expect(remainingSeconds('2026-11-22T15:09:59',at)).toBe(599);expect(timeLeft(599)).toBe('09:59')});
  it('deshabilita el reloj luego del vencimiento',()=>{expect(remainingSeconds('2020-01-01T00:00:00')).toBe(0)});
  it('valida identificadores antes de enviarlos al servidor',()=>{expect(integer('0')).toBeNull();expect(integer('1.5')).toBeNull();expect(integer('abc')).toBeNull();expect(integer('17')).toBe(17)});
});
