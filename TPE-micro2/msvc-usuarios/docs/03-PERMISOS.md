# Matriz de permisos aplicada

P = público; C = CLIENTE; O = ORGANIZADOR; A = ADMINISTRADOR.
S_eventos / S_reservas / S_compras / S_entradas son identidades de servicio,
no roles asignables a personas. Tener varios roles acumula permisos.
Toda ruta/método no autorizado explícitamente se deniega. Un JWT inválido produce
401 incluso en una ruta pública si se envía el header Bearer.

La matriz se aplica tanto en Gateway como en el servicio. La titularidad y el
estado publicado se comprueban en el servicio que posee la base de datos.

| Método | Endpoint real | Permiso adicional al JWT válido |
|---|---|---|
| POST | /auth/register, /auth/login | P (login valida credenciales y límites) |
| POST | /auth/service-token | Credencial HTTP Basic configurada |
| GET | /usuarios/me | Persona autenticada, cualquiera de los cuatro roles |
| GET | /usuarios/{id} | Titular o A |
| PATCH | /usuarios/{id}/roles, /usuarios/{id}/estado | A |
| GET | /eventos/publicados | P; sólo PUBLICADO/AGOTADO |
| GET | /eventos/buscar | P; incluirNoPublicados=true requiere A |
| GET | /eventos | A |
| GET | /eventos/{id} | P si PUBLICADO/AGOTADO; O dueño, A o S_reservas/S_compras/S_entradas para los otros estados |
| GET | /eventos/{id}/sectores, /eventos/{id}/habilitado | Igual regla de visibilidad del evento |
| GET | /eventos/{eventoId}/sectores/{sectorId}/precio | Igual regla de visibilidad del evento |
| POST | /eventos | O vinculado al organizador indicado o A |
| PUT | /eventos/{id} | O dueño y vinculado al organizador destino, o A |
| PUT | /eventos/{id}/precios | O dueño o A |
| PATCH | /eventos/{id}/publicar, /agotar, /reabrir, /finalizar | O dueño o A; las reglas de estado siguen en Eventos |
| PATCH | /eventos/{id}/suspender, /cancelar | A |
| GET | /eventos/{id}/notificaciones-cancelacion | A |
| POST | /organizadores | A |
| GET | /organizadores | A (incluye emails comerciales) |
| PATCH | /organizadores/{id}/usuario | A; vinculación explícita con usuarioId |
| GET | /lugares, /lugares/{id}, /lugares/{id}/sectores | P |
| GET | /sectores/{id}, /sectores/{id}/asientos, /asientos/{id} | P |
| GET | /participantes | P |
| POST | /lugares, /lugares/{id}/sectores, /sectores/{id}/asientos, /participantes | A (infraestructura compartida sin propietario modelado) |
| GET | /disponibilidad?eventoId=&sectorId= | P; valida reglas de disponibilidad existentes |
| POST | /reservas | C; clienteId = sub |
| GET | /reservas/{id} | C dueño, A, S_compras o S_entradas |
| POST | /reservas/{id}/confirmar, /liberar | S_compras exclusivamente |
| POST | /reservas/{id}/devolver-entrada | S_entradas exclusivamente |
| POST | /reservas/evento/{eventoId}/habilitar, /cancelar | S_eventos exclusivamente |
| POST | /compras | C; clienteId = sub y dueño de reserva |
| GET | /compras/{id} | C dueño o A |
| GET | /compras/{id}/confirmacion | C dueño, A o S_entradas |
| POST | /compras/{id}/pagar, /confirmar, /reintentar-liberacion | C dueño (también se exige dueño a una cuenta con roles C+A) |
| GET | /compras/{id}/reintegros | C dueño o A |
| POST | /compras/{id}/compensar | A |
| POST | /compras/reintegros | S_entradas exclusivamente |
| POST | /compras/evento/{eventoId}/cancelar | S_eventos exclusivamente |
| POST | /entradas/emitir | S_compras exclusivamente |
| GET | /entradas/{id} | C dueño; X-Cliente-Id debe coincidir con sub y la entrada. A y S_compras deben indicar el titular real en ese header |
| GET | /entradas/cliente/{clienteId} | C propio, A o S_compras |
| POST | /entradas/evento/{id}/invalidar | S_eventos o S_compras |
| POST | /devoluciones | C dueño de la entrada |
| GET | /devoluciones/pendientes | A |
| PATCH | /devoluciones/{id}/aprobar, /rechazar | A; administradorId se obtiene de sub, parámetro legado opcional sólo si coincide |
| GET | /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html | P |
| GET | /actuator/health | P en aplicaciones que exponen Actuator |
| GET | /usuarios/v3/api-docs y documentación de otros servicios en Gateway | P, reescritura a /v3/api-docs |

PERSONAL_ACCESO puede consultar su perfil y usar las consultas públicas. No existe
en esta versión un endpoint de validación/uso de QR en Entradas: no se inventó uno
ni se le dieron lecturas de tickets completos que exponen QR e identidad del cliente.
Al incorporarlo, debe devolver datos mínimos, validar estado/uso atómicamente y
restringirse a PERSONAL_ACCESO/A y a la asignación al evento que defina el equipo.

Los organizadores configuran sectores/precios **de sus eventos** mediante
/eventos/{id}/precios. Crear sectores físicos compartidos requiere A porque
Lugar/Sector no tienen propietario y un cambio afectaría a eventos ajenos.

No hay endpoints de reportes, preferencias o fila virtual en el código relevado.
Esta matriz no afirma que esas funcionalidades estén implementadas.
