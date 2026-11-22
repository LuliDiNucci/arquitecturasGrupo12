# 01 — Funcionalidades e historias de usuario

## Objetivo

El microservicio Fila Virtual organiza el acceso a eventos de alta demanda mediante turnos temporales. Permite administrar el orden de espera y verificar qué clientes están autorizados para crear una reserva.

## Historias de usuario

### US01 — Ingresar a la fila virtual

**Como cliente**, quiero ingresar a la fila de un evento de alta demanda para esperar mi turno.

**Criterios de aceptación:**
- El evento debe estar `PUBLICADO` y configurado como de alta demanda.
- El cliente debe estar autenticado con JWT.
- Se registra el orden de ingreso.
- Si el cliente ya está registrado, se devuelve el registro existente sin duplicarlo.
- Un cliente solamente puede ingresar en su propio nombre.

### US02 — Consultar posición y estado

**Como cliente**, quiero conocer mi posición y el estado de mi turno.

**Criterios de aceptación:**
- Se informa el estado actual del cliente.
- Mientras está `EN_ESPERA`, se calcula su posición entre quienes continúan esperando.
- Si está habilitado, se informa el vencimiento de su autorización.
- El cliente consulta sus propios datos; un administrador puede consultar otros registros.

### US03 — Habilitar una tanda

**Como administrador**, quiero habilitar grupos de clientes respetando el orden de llegada.

**Criterios de aceptación:**
- Se habilitan los primeros clientes en espera.
- Cada tanda admite entre 1 y 100 clientes.
- Las autorizaciones tienen una duración de 10 minutos.
- No se habilita una segunda tanda mientras exista otra activa.
- Se utilizan mecanismos transaccionales para controlar solicitudes simultáneas.

### US04 — Validar un turno

**Como sistema de Reservas**, quiero comprobar que un cliente tenga autorización vigente antes de crear su reserva.

**Criterios de aceptación:**
- La operación requiere credenciales internas de Reservas.
- Se verifica el estado `HABILITADO` y el vencimiento del turno.
- La validación devuelve si el cliente está autorizado.
- Consultar el turno no consume la autorización.

### US05 — Gestionar vencimientos

**Como sistema**, quiero vencer automáticamente las autorizaciones que superaron su tiempo permitido.

**Criterios de aceptación:**
- Un turno habilitado vence después de 10 minutos.
- Los turnos vencidos pasan a `VENCIDO`.
- El proceso de vencimiento se ejecuta periódicamente.
- Un turno vencido no autoriza nuevas reservas.

### US06 — Finalizar un turno utilizado

**Como sistema de Reservas**, quiero finalizar un turno después de crear correctamente la reserva temporal.

**Criterios de aceptación:**
- La finalización se realiza mediante una operación interna autorizada.
- El turno pasa a `FINALIZADO` y no puede reutilizarse.
- Se permite finalizar un turno `VENCIDO` cuando Reservas comunica tardíamente una reserva ya creada.
- La operación es idempotente.
- Reservas conserva y reintenta las finalizaciones que no pudieron comunicarse inmediatamente.