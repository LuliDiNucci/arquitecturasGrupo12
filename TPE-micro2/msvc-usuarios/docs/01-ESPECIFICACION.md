# Entrega 2: identidad y seguridad

## Alcance y relevamiento previo

Se incorpora un Maven independiente Java 21 / Spring Boot 3.5.6, paquete
`com.ticketya.usuarios`, puerto 8085. Gateway y Eventos conservan Boot 3.5.6;
Reservas, Compras y Entradas conservan Boot 4.1.2. Gateway usa WebFlux y los
otros servicios usan MVC. No existía configuración de seguridad ni Compose.

Los controllers reales usan `clienteId` numérico en Reservas/Compras y
`X-Cliente-Id` en Entradas. El contrato será `clienteId = Long.valueOf(jwt.sub)`:
se comprueba contra el principal y contra el recurso persistido. Los datos
históricos requieren asociarse a usuarios reales antes de usarlos.
`Organizador.id` es una identidad de negocio distinta: se agregará una relación
explícita `Organizador.usuarioId`, asignable exclusivamente por administración.
Los lugares, sectores y asientos son infraestructura compartida: su modificación
queda reservada a administración; el organizador configura precios de su evento.

## Diseño previsto

- Registro público asigna solamente CLIENTE; email canónico y único en SQL;
  BCrypt y DTO sin hashes. Login uniforme para credenciales inválidas/inactivas.
- JWT RS256 firmado solamente en Usuarios; claves PEM externas, `iss`, `aud`,
  `sub`, `iat`, `exp`, `jti`, `roles` y `token_use` obligatorios y validados.
- Autorización por método/ruta en Gateway y cada servicio; propiedad comprobada
  antes de invocar operaciones de negocio. Rutas desconocidas denegadas.
- Credenciales por servicio para obtener JWT de cinco minutos mediante Basic
  en `/auth/service-token`. No son roles de usuario: cada identidad de servicio
  recibe únicamente sus operaciones internas. Feign/RestClient propagan el JWT
  del usuario en consultas y usan su propia credencial para operaciones internas
  y tareas programadas. No se publican endpoints internos anónimos.
- Primer administrador mediante bootstrap optativo con variables de entorno,
  habilitado solamente sobre una base sin usuarios; nunca un registro público.
- Pruebas con H2, RSA generada durante el test, requests HTTP reales/MockMvc y
  stubs de integraciones; Docker y documentación reproducible.

## Historias y criterios de aceptación

1. Como visitante puedo registrarme (201), recibir 400 por datos inválidos y
   409 por email duplicado. No puedo elegir privilegios ni obtener hashes.
2. Como usuario activo puedo ingresar y acceder a mis datos mediante Bearer.
   Credenciales erróneas producen 401 e intentos reiterados 429 temporal.
3. Como administrador puedo asignar los cuatro roles exactos y desactivar
   cuentas. Propietarios pueden consultar sus datos; otros reciben 403.
4. Como cliente opero solamente sobre mis reservas, compras, entradas y
   devoluciones. Como organizador modifico/publico solamente mis eventos.
5. Como personal de acceso no accedo a compras ni administración. El código
   original todavía no tiene un endpoint de validación de ingreso QR.
6. Gateway y acceso directo rechazan JWT inválidos/expirados (401), rechazan
   permisos insuficientes (403) y mantienen públicas las consultas publicadas.
7. Confirmación, emisión, devolución y cancelación siguen autenticadas entre
   servicios, incluso en reintentos programados sin sesión humana.
8. Compilación, pruebas y empaquetado de los seis servicios se registran con
   resultados reales; cualquier limitación de infraestructura queda documentada.

La matriz exhaustiva y los contratos finales se documentan junto al código;
esta especificación precede los cambios de implementación.
