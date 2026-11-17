param([string]$MavenRepository)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$runRoot = Join-Path $repoRoot ('.local-security-smoke/run-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$null = New-Item -ItemType Directory -Path $runRoot -Force
& java (Join-Path $PSScriptRoot 'GenerateDevSecrets.java') $runRoot
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron generar claves efímeras para la prueba' }
$settings = @{}
foreach ($line in Get-Content -LiteralPath (Join-Path $runRoot '.env')) {
    if ($line -match '^[A-Z][A-Z0-9_]*=') { $pair = $line -split '=', 2; $settings[$pair[0]] = $pair[1] }
}
$services = @(
    @{ id = 'usuarios'; dir = 'msvc-usuarios'; main = 'com.ticketya.usuarios.UsuariosApplication'; port = 18085 },
    @{ id = 'eventos'; dir = 'msvc-eventosylugares'; main = 'ar.edu.unicen.ticketya.eventos.TicketYaEventosApplication'; port = 18081 },
    @{ id = 'reservas'; dir = 'msvc-reservas'; main = 'com.ticketya.reservas.ReservasApplication'; port = 18080 },
    @{ id = 'compras'; dir = 'msvc-compras'; main = 'com.ticketya.compras.ComprasApplication'; port = 18082 },
    @{ id = 'entradas'; dir = 'msvc-entradas'; main = 'com.ticketya.entradas.EntradasApplication'; port = 18083 },
    @{ id = 'gateway'; dir = 'msvc-gateway'; main = 'com.ticketya.gateway.MsvcGatewayApplication'; port = 18084 }
)
$processes = @()
$savedEnv = @{}
function Set-SmokeEnv([string]$Name, [string]$Value) {
    if (-not $savedEnv.ContainsKey($Name)) { $savedEnv[$Name] = [Environment]::GetEnvironmentVariable($Name, 'Process') }
    [Environment]::SetEnvironmentVariable($Name, $Value, 'Process')
}
$script:checks = 0
function Check-Http([string]$Method, [string]$Path, [int]$Expected, $Body = $null, [string]$Token = '', [hashtable]$ExtraHeaders = @{}) {
    $headers = @{}
    foreach ($key in $ExtraHeaders.Keys) { $headers[$key] = $ExtraHeaders[$key] }
    if ($Token) { $headers['Authorization'] = 'Bearer ' + $Token }
    $uri = if ($Path.StartsWith('http')) { $Path } else { 'http://localhost:18084' + $Path }
    $arguments = @{ Method = $Method; Uri = $uri; Headers = $headers; UseBasicParsing = $true; TimeoutSec = 20 }
    if ($null -ne $Body) {
        $arguments['ContentType'] = 'application/json'
        $arguments['Body'] = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 15 -Compress))
    }
    $content = ''
    try {
        $response = Invoke-WebRequest @arguments
        $status = [int]$response.StatusCode
        $content = $response.Content
    } catch {
        if ($null -eq $_.Exception.Response) { throw }
        $status = [int]$_.Exception.Response.StatusCode
    }
    if ($status -ne $Expected) { throw ($Method + ' ' + $Path + ': HTTP ' + $status + ', esperado ' + $Expected) }
    $script:checks++
    if ($content) { return ($content | ConvertFrom-Json) }
}
try {
    foreach ($service in $services) {
        $serviceRoot = Join-Path $repoRoot $service.dir
        Push-Location $serviceRoot
        try {
            $mavenArgs = @('-B')
            if ($MavenRepository) { $mavenArgs += '-Dmaven.repo.local=' + $MavenRepository }
            $mavenArgs += @('-DincludeScope=test', '-Dmdep.outputFile=target/smoke-classpath.txt', 'test-compile', 'dependency:build-classpath')
            $previousErrorPreference = $ErrorActionPreference
            $ErrorActionPreference = 'Continue'
            try { & mvn @mavenArgs *> (Join-Path $runRoot ($service.id + '-build.log')) }
            finally { $ErrorActionPreference = $previousErrorPreference }
            if ($LASTEXITCODE -ne 0) { throw ('Falló la preparación de ' + $service.id + '; revisar ' + $runRoot) }
        } finally { Pop-Location }
        Set-SmokeEnv 'PORT' ([string]$service.port)
        Set-SmokeEnv 'SPRING_PROFILES_ACTIVE' 'smoke'
        Set-SmokeEnv 'JWT_PUBLIC_KEY' ('file:' + (Join-Path $runRoot '.secrets/jwt-public.pem').Replace('\', '/'))
        Set-SmokeEnv 'JWT_PRIVATE_KEY' ('file:' + (Join-Path $runRoot '.secrets/jwt-private.pem').Replace('\', '/'))
        Set-SmokeEnv 'JWT_ISSUER' 'ticketya-usuarios'
        Set-SmokeEnv 'JWT_AUDIENCE' 'ticketya-api'
        Set-SmokeEnv 'SPRING_DATASOURCE_URL' ('jdbc:h2:mem:smoke_' + $service.id + ';DB_CLOSE_DELAY=-1;MODE=MySQL')
        Set-SmokeEnv 'SPRING_DATASOURCE_DRIVER_CLASS_NAME' 'org.h2.Driver'
        Set-SmokeEnv 'SPRING_DATASOURCE_USERNAME' 'sa'
        Set-SmokeEnv 'SPRING_DATASOURCE_PASSWORD' 'smoke-only-password'
        Set-SmokeEnv 'DB_PASSWORD' 'smoke-only-password'
        Set-SmokeEnv 'SPRING_JPA_HIBERNATE_DDL_AUTO' 'create-drop'
        Set-SmokeEnv 'SPRING_JPA_SHOW_SQL' 'false'
        Set-SmokeEnv 'APP_INTEGRACIONES_HABILITADAS' 'true'
        Set-SmokeEnv 'APP_INTEGRACIONES_REINTENTO_MS' '1000'
        foreach ($target in $services | Where-Object { $_.id -ne 'gateway' }) {
            Set-SmokeEnv ($target.id.ToUpperInvariant() + '_URL') ('http://localhost:' + $target.port)
            if ($target.id -in @('reservas', 'compras', 'entradas')) {
                Set-SmokeEnv ('APP_INTEGRACIONES_' + $target.id.ToUpperInvariant() + '_BASE_URL') ('http://localhost:' + $target.port)
            }
        }
        foreach ($key in @('SERVICE_EVENTOS_SECRET', 'SERVICE_RESERVAS_SECRET', 'SERVICE_COMPRAS_SECRET', 'SERVICE_ENTRADAS_SECRET')) {
            Set-SmokeEnv $key $(if ($service.id -eq 'usuarios') { $settings[$key] } else { '' })
        }
        Set-SmokeEnv 'SERVICE_CLIENT_SECRET' $(if ($service.id -in @('eventos', 'reservas', 'compras', 'entradas')) { $settings['SERVICE_' + $service.id.ToUpperInvariant() + '_SECRET'] } else { '' })
        Set-SmokeEnv 'SERVICE_TOKEN_URL' 'http://localhost:18085/auth/service-token'
        Set-SmokeEnv 'BOOTSTRAP_ADMIN_ENABLED' $(if ($service.id -eq 'usuarios') { 'true' } else { 'false' })
        Set-SmokeEnv 'BOOTSTRAP_ADMIN_EMAIL' $settings['BOOTSTRAP_ADMIN_EMAIL']
        Set-SmokeEnv 'BOOTSTRAP_ADMIN_PASSWORD' $settings['BOOTSTRAP_ADMIN_PASSWORD']
        $classpath = (Join-Path $serviceRoot 'target/classes') + ';' + (Get-Content -LiteralPath (Join-Path $serviceRoot 'target/smoke-classpath.txt') -Raw).Trim()
        $process = Start-Process -FilePath 'java.exe' -ArgumentList @('-Xmx192m', '-cp', ('"' + $classpath + '"'), $service.main) -WorkingDirectory $serviceRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $runRoot ($service.id + '.log')) -RedirectStandardError (Join-Path $runRoot ($service.id + '-stderr.log'))
        $processes += $process
        $started = $false
        for ($attempt = 0; $attempt -lt 90; $attempt++) {
            if ($process.HasExited) { throw ($service.id + ' terminó durante el inicio; revisar ' + $runRoot) }
            try {
                $probe = if ($service.id -eq 'gateway') { '/actuator/health' } else { '/v3/api-docs' }
                $response = Invoke-WebRequest -UseBasicParsing -Uri ('http://localhost:' + $service.port + $probe) -TimeoutSec 2
                if ([int]$response.StatusCode -eq 200) { $started = $true; break }
            } catch { Start-Sleep -Seconds 1 }
        }
        if (-not $started) { throw ('Tiempo de inicio excedido: ' + $service.id) }
        Write-Output ('Inició ' + $service.id)
    }
    $adminLogin = Check-Http POST '/auth/login' 200 @{ email = $settings['BOOTSTRAP_ADMIN_EMAIL']; password = $settings['BOOTSTRAP_ADMIN_PASSWORD'] }
    $adminToken = $adminLogin.accessToken
    $password = 'Contrasena-smoke-2026!'
    $user = Check-Http POST '/auth/register' 201 @{ nombre = 'Cliente'; apellido = 'Smoke'; email = 'cliente@example.com'; password = $password; roles = @('ADMINISTRADOR') }
    if (@($user.roles) -join ',' -ne 'CLIENTE') { throw 'Registro permitió escalar roles' }
    $clientLogin = Check-Http POST '/auth/login' 200 @{ email = $user.email; password = $password }
    $clientToken = $clientLogin.accessToken
    $other = Check-Http POST '/auth/register' 201 @{ nombre = 'Otra'; apellido = 'Persona'; email = 'otra@example.com'; password = $password }
    $null = Check-Http GET '/usuarios/me' 200 $null $clientToken
    $null = Check-Http GET ('/usuarios/' + $other.id) 403 $null $clientToken
    $null = Check-Http GET '/usuarios/me' 401
    $null = Check-Http GET '/usuarios/me' 401 $null 'alterado'
    $null = Check-Http GET '/usuarios/v3/api-docs' 200
    $null = Check-Http GET '/eventos/publicados' 200
    $null = Check-Http POST '/eventos' 403 @{} $clientToken
    $null = Check-Http PATCH '/devoluciones/1/aprobar' 403 $null $clientToken
    $null = Check-Http GET 'http://localhost:18080/reservas/1' 401
    $null = Check-Http GET 'http://localhost:18082/compras/1' 401
    $null = Check-Http GET 'http://localhost:18083/entradas/cliente/1' 401
    $organizer = Check-Http POST '/auth/register' 201 @{ nombre = 'Organizador'; apellido = 'Smoke'; email = 'org@example.com'; password = $password }
    $null = Check-Http PATCH ('/usuarios/' + $organizer.id + '/roles') 200 @{ roles = @('ORGANIZADOR') } $adminToken
    $orgLogin = Check-Http POST '/auth/login' 200 @{ email = $organizer.email; password = $password }
    $orgToken = $orgLogin.accessToken
    $business = Check-Http POST '/organizadores' 201 @{ nombre = 'Producciones Smoke'; email = 'producciones@example.com' } $adminToken
    $null = Check-Http PATCH ('/organizadores/' + $business.id + '/usuario') 200 @{ usuarioId = $organizer.id } $adminToken
    $participant = Check-Http POST '/participantes' 201 @{ nombre = 'Banda'; tipo = 'ARTISTA' } $adminToken
    $place = Check-Http POST '/lugares' 201 @{ nombre = 'Teatro'; direccion = 'Calle 1'; ciudad = 'Tandil'; latitud = -37.32; longitud = -59.14 } $adminToken
    $sector = Check-Http POST ('/lugares/' + $place.id + '/sectores') 201 @{ nombre = 'Campo'; numerado = $false; capacidad = 10 } $adminToken
    $event = Check-Http POST '/eventos' 201 @{ nombre = 'Recital smoke'; descripcion = 'Prueba de seguridad'; tipo = 'RECITAL'; organizadorId = $business.id; lugarId = $place.id; fechaHora = (Get-Date).AddMonths(2).ToString('yyyy-MM-ddTHH:mm:ss'); participantesIds = @($participant.id); altaDemanda = $false } $orgToken
    $null = Check-Http PUT ('/eventos/' + $event.id + '/precios') 200 @{ precios = @(@{ sectorId = $sector.id; precio = 1000 }) } $orgToken
    $null = Check-Http PATCH ('/eventos/' + $event.id + '/publicar') 200 $null $orgToken
    $reservation = Check-Http POST '/reservas' 201 @{ clienteId = $user.id; eventoId = $event.id; claveIdempotencia = 'smoke-reserva'; detalles = @(@{ sectorId = $sector.id; cantidad = 2; asientoIds = @() }) } $clientToken
    $otherLogin = Check-Http POST '/auth/login' 200 @{ email = $other.email; password = $password }
    $null = Check-Http POST '/compras' 403 @{ clienteId = $other.id; reservaId = $reservation.id } $otherLogin.accessToken
    $purchase = Check-Http POST '/compras' 201 @{ clienteId = $user.id; reservaId = $reservation.id } $clientToken
    $null = Check-Http POST ('/compras/' + $purchase.id + '/pagar') 200 @{ medioPago = 'TARJETA'; claveIdempotencia = 'smoke-pago' } $clientToken
    $null = Check-Http POST ('/compras/' + $purchase.id + '/confirmar') 200 $null $clientToken
    $entries = @(Check-Http GET ('/entradas/cliente/' + $user.id) 200 $null $clientToken)
    if ($entries.Count -ne 2) { throw 'No se emitieron las dos entradas esperadas' }
    $null = Check-Http GET ('/entradas/cliente/' + $other.id) 403 $null $clientToken
    $null = Check-Http GET ('/entradas/' + $entries[0].id) 403 $null $clientToken @{ 'X-Cliente-Id' = [string]$other.id }
    $null = Check-Http GET ('/entradas/' + $entries[0].id) 200 $null $clientToken @{ 'X-Cliente-Id' = [string]$user.id }
    $return = Check-Http POST '/devoluciones' 200 @{ entradaId = $entries[0].id; motivo = 'No puedo asistir' } $clientToken
    $null = Check-Http PATCH ('/devoluciones/' + $return.id + '/aprobar') 200 $null $adminToken
    $refunds = @(Check-Http GET ('/compras/' + $purchase.id + '/reintegros') 200 $null $clientToken)
    if ($refunds.Count -ne 1) { throw 'No se procesó el reintegro individual' }
    $null = Check-Http PATCH ('/eventos/' + $event.id + '/cancelar') 200 $null $adminToken
    $completed = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        $refunds = @(Check-Http GET ('/compras/' + $purchase.id + '/reintegros') 200 $null $clientToken)
        if ($refunds.Count -eq 2) { $completed = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $completed) { throw 'No se completó la notificación programada de cancelación y reintegro' }
    $report = @{ checks = $script:checks; result = 'PASS'; database = 'H2 en memoria, una por servicio'; flow = 'registro-login-gateway-publicacion-reserva-pago-emision-devolucion-cancelacion' }
    $report | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $runRoot 'result.json') -Encoding UTF8
    Write-Output ('PASS: ' + $script:checks + ' verificaciones HTTP. Reporte: ' + $runRoot)
} finally {
    foreach ($process in $processes) { if (-not $process.HasExited) { Stop-Process -Id $process.Id -ErrorAction SilentlyContinue } }
    foreach ($key in $savedEnv.Keys) { [Environment]::SetEnvironmentVariable($key, $savedEnv[$key], 'Process') }
}
