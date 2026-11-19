# Guía reproducible de ejecución

Los comandos parten de **`TPE-micro2`**, no del directorio de ejercicios del IDE ni
de `TPE-micro1`. Requisitos: Java 21, Maven, Docker con motor Linux iniciado y Docker
Compose. PowerShell se usa para los scripts de verificación; los comandos Maven y
Docker también pueden ejecutarse directamente en otras terminales.

## 1. Preparar claves y configuración

```powershell
cd C:\Users\usuario\Downloads\ESTUDIO_FINAL_\arquitecturasGrupo12\TPE-micro2
java scripts/GenerateDevSecrets.java
New-Item -ItemType Directory -Force logs | Out-Null
```

El generador crea RSA 3072, siete contraseñas de MySQL, cuatro credenciales de servicio,
administrador de Usuarios y contraseña de Grafana. No imprime secretos y se niega a
sobrescribir una configuración existente. Si ya hay `.env`/`.secrets`, conservarlos y
completar las variables faltantes de `.env.example` con valores locales nuevos.
Grafana exige `GRAFANA_ADMIN_PASSWORD`. Nunca copiar claves de los directorios de tests
a una instalación propia ni versionar archivos generados.

## 2. Compilar y ejecutar todas las pruebas

```powershell
mvn -B clean install
```

El agregador compila primero `observability`. No cambia las versiones de Boot/Cloud
de los servicios. Para compilar sólo un servicio por separado:

```powershell
mvn -B -f observability/pom.xml install
mvn -B -f msvc-notificaciones/pom.xml verify
```

Alternativa con un reporte de salida por módulo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Verify-Security.ps1
```

En este equipo el caché Maven real requiere especificarlo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Verify-Security.ps1 -MavenRepository C:\Users\usuario\.m2\repository -Offline
```

Omitir `-Offline` si faltan dependencias. Los resultados de Surefire están en
`<modulo>/target/surefire-reports`, y las salidas en `<modulo>/build-install.log`.
El script conserva tests y seguridad; `install` incluye compilación, pruebas,
empaquetado y `verify`. Para un módulo: `-Service notificaciones`, `-Service gateway`,
etc.; también instala y verifica primero la biblioteca común.

## 3. Ejecutar todo con Docker

```powershell
docker version
docker compose -f docker-compose.yml -f docker-compose.observability.yml config --quiet
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.observability.yml ps
```

Las imágenes de aplicación se construyen desde el contexto `TPE-micro2` para incluir
la biblioteca. No ejecutar `docker build msvc-compras` como antes; para una sola imagen:

```powershell
docker build -f msvc-compras/Dockerfile -t ticketya-compras .
```

Los Dockerfiles mantienen la omisión de ejecución de tests durante la creación de
imagen; se ejecutan con Maven en el paso anterior. Se conservan los volúmenes de bases
del Compose existente y se agregan otros separados para logs, Loki, Alloy y Grafana.
No hace falta reiniciar los servicios si se reinicia sólo la observabilidad.
`logs-init` termina con código 0 después de preparar permisos del volumen: ese estado
es el esperado. Java se ejecuta con UID 10001 y espera a esa inicialización.

- Gateway y Swagger agregado: `http://localhost:8084/swagger-ui.html`.
- Grafana: `http://localhost:3000`, usuario de `GRAFANA_ADMIN_USER` y contraseña local
  `GRAFANA_ADMIN_PASSWORD`. Explore → datasource **TicketYa Logs**.
- Loki readiness: `http://localhost:3100/ready`.
- Mailpit: `http://localhost:8025` para observar correos del escenario Docker.

Si una base existente de Eventos usa el perfil MariaDB con `ddl-auto=validate`, aplicar
una vez el SQL indicado en [07](07-OBSERVABILIDAD.md) antes de actualizar la aplicación.
Los perfiles de desarrollo MySQL mantienen `update`; no se borran datos para agregar
la correlación ni la tabla de asignaciones de Entradas.

Para verificar los formatos nativos de los dos componentes cuando el motor esté iniciado:

```powershell
docker compose -f docker-compose.observability.yml run --rm --no-deps loki -config.file=/etc/loki/ticketya.yml -verify-config=true
docker compose -f docker-compose.observability.yml run --rm --no-deps alloy validate /etc/alloy/config.alloy
```

Esos dos validadores y la ingesta real **no se ejecutaron en el entorno de entrega**, que
no tenía el motor Docker iniciado. La validación sintáctica de Compose sí se ejecutó.

## 4. Ejecutar servicios locales con recolección central

Para servicios iniciados por Maven/IDE o para el smoke se levanta sólo observabilidad:

```powershell
docker compose -f docker-compose.observability.yml up -d
```

Alloy monta `TPE-micro2/logs` sólo para lectura. Si se inicia Maven desde cada carpeta
de módulo se usa esa carpeta automáticamente. Desde cualquier otra ubicación, configurar:

```powershell
$env:TICKETYA_LOG_DIR = 'C:\Users\usuario\Downloads\ESTUDIO_FINAL_\arquitecturasGrupo12\TPE-micro2\logs'
```

Los otros valores de ejecución local (base, URLs, claves y `SERVICE_CLIENT_SECRET`)
siguen siendo los de cada servicio. Maven no importa `.env` automáticamente; se deben
pasar las variables al proceso del IDE/terminal. Sólo Usuarios recibe la clave privada
y los cuatro secretos de emisores; cada servicio emisor recibe exclusivamente el suyo.
No se debe importar todo `.env` indistintamente en todos los procesos.

Si se cambió `TICKETYA_LOG_DIR`, adaptar el bind mount local de Alloy a esa carpeta.
Consola sin archivo no se recolecta. Docker usa el volumen `application-logs` y local
usa la carpeta `logs`; ambos están configurados como fuentes separadas.

## 5. Circuito completo de ejemplo sin instalar MySQL/SMTP

El smoke usa siete procesos reales: Usuarios, Eventos, Reservas, Compras, Entradas,
Notificaciones y Gateway. Cada aplicación dispone de H2 propia, con la seguridad
normal activa, claves y secretos efímeros. Un proceso GreenMail recibe el correo por
SMTP. No requiere ni modifica las bases del Compose.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Smoke-Security.ps1
```

En este equipo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Smoke-Security.ps1 -MavenRepository C:\Users\usuario\.m2\repository
```

El script crea administrador, clientes y organizador, publica un evento, reserva,
compra, paga, emite dos entradas, devuelve una, cancela el evento y verifica reintegros.
Otro evento independiente permite probar QR: CLIENTE → 403; personal sin asignar →
403; administrador asigna → 204; personal valida → 200; segundo uso → 409; revocación
→ 204; siguiente intento → 403. Comprueba notificaciones propias/ajenas y correo enviado.

Finalmente detiene sólo su proceso de Eventos y comprueba el 503 de Reservas y su
registro correlacionado. Todos los procesos que creó se detienen al salir, también
si falla una aserción. No detiene servicios que estuvieran ejecutándose previamente.

Reservar los puertos `18080` a `18085`, `18087` y `18025` para este script. No inicia
Recomendaciones/Mongo ni Fila Virtual: sus tests se ejecutan con Maven por separado.

La salida informa el directorio `.local-security-smoke/run-<fecha>` con:

- `result.json`: estado, cantidad de checks e IDs de correlación.
- `trace-evidence.json`: registros JSON efectivamente leídos de los logs.
- `<servicio>.log`, `<servicio>-stderr.log`, `<servicio>-build.log`: salida reproducible.
- `.env`/`.secrets` temporales, ignorados por Git y nunca incluidos en esta documentación.

Para verificar además Loki, con la observabilidad del paso 4 ya iniciada:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Smoke-Security.ps1 -VerifyLoki
```

El verificador falla si no puede consultar Loki o no encuentra al menos dos servicios.
No considera exitoso el paso de centralización sólo por haber creado los contenedores.

## 6. Requests manuales mínimos

Con las aplicaciones del Compose iniciadas, leer únicamente los datos necesarios para
el login local del administrador, sin imprimir contraseñas ni tokens:

```powershell
$localConfig = @{}
Get-Content .env | ForEach-Object {
    if ($_ -match '^[A-Z][A-Z0-9_]*=') {
        $pair = $_ -split '=', 2
        $localConfig[$pair[0]] = $pair[1]
    }
}
$loginBody = @{
    email = $localConfig['BOOTSTRAP_ADMIN_EMAIL']
    password = $localConfig['BOOTSTRAP_ADMIN_PASSWORD']
} | ConvertTo-Json
$trace = 'defensa-001'
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8084/auth/login' -ContentType 'application/json' -Body $loginBody -Headers @{'X-Correlation-ID'=$trace}
$headers = @{Authorization=('Bearer ' + $login.accessToken); 'X-Correlation-ID'=$trace}
$profile = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8084/usuarios/me' -Headers $headers
$profile.StatusCode
$profile.Headers['X-Correlation-ID']
```

En Grafana Explore:

```logql
{job="ticketya"} | json | correlationId="defensa-001"
```

Para una compra completa, ejecutar el smoke del paso 5 y usar el `correlationId` de
su `result.json`. Incluye Gateway, Compras, Reservas, Eventos, Entradas, Usuarios y
Notificaciones; para el fallo buscar `errorCorrelationId`. Se puede automatizar esa
consulta sobre logs reales del último smoke:

```powershell
$lastRun = Get-ChildItem .local-security-smoke -Directory | Sort-Object Name -Descending | Select-Object -First 1
$result = Get-Content (Join-Path $lastRun.FullName 'result.json') -Raw | ConvertFrom-Json
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Verify-CentralizedLogs.ps1 -CorrelationId $result.correlationId
```

El script conserva la respuesta de `/loki/api/v1/query_range` en
`.local-observability/loki-<id>.json`. El intervalo predeterminado es de los últimos
30 minutos; se puede cambiar con `-Minutes`.

En Postman, conservar `Authorization: Bearer <accessToken>` para operaciones de usuario
y añadir `X-Correlation-ID: defensa-compra-001` a los pasos de una misma operación.
Las colecciones JWT existentes siguen siendo válidas. Los cuatro POST de Notificaciones
requieren un token de servicio obtenido directamente de Usuarios con las credenciales
del emisor correspondiente. Swagger mantiene los contratos y muestra Bearer JWT.

## 7. Detener

```powershell
docker compose -f docker-compose.yml -f docker-compose.observability.yml down
```

Se conservan datos y offsets en volúmenes. No usar `down -v` para reiniciar: también
borraría las bases de datos existentes. Los logs de archivo tienen rotación y Loki
retención; si Alloy queda apagado durante una rotación, revisar la continuidad de
lectura antes de asumir que se preservó todo el historial.
