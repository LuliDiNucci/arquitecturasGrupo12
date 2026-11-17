param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('usuarios', 'gateway', 'eventos', 'reservas', 'compras', 'entradas')]
    [string]$Service,
    [string]$MavenRepository
)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$envFile = Join-Path $repoRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) { throw 'Ejecute primero: java scripts/GenerateDevSecrets.java' }
$settings = @{}
foreach ($line in Get-Content -LiteralPath $envFile -Encoding UTF8) {
    if ($line -match '^[A-Z][A-Z0-9_]*=') {
        $pair = $line -split '=', 2
        $settings[$pair[0]] = $pair[1]
    }
}
$ports = @{ usuarios = 8085; gateway = 8084; eventos = 8081; reservas = 8080; compras = 8082; entradas = 8083 }
$dbPorts = @{ usuarios = 3307; eventos = 3308; reservas = 3309; compras = 3310; entradas = 3311 }
$directories = @{ usuarios = 'msvc-usuarios'; gateway = 'msvc-gateway'; eventos = 'msvc-eventosylugares'; reservas = 'msvc-reservas'; compras = 'msvc-compras'; entradas = 'msvc-entradas' }
$bindings = @{
    PORT = [string]$ports[$Service]
    JWT_PUBLIC_KEY = 'file:' + (Join-Path $repoRoot '.secrets/jwt-public.pem').Replace('\', '/')
    JWT_ISSUER = 'ticketya-usuarios'
    JWT_AUDIENCE = 'ticketya-api'
}
foreach ($target in @('usuarios', 'eventos', 'reservas', 'compras', 'entradas')) {
    $bindings[$target.ToUpperInvariant() + '_URL'] = 'http://localhost:' + $ports[$target]
}
if ($Service -ne 'gateway') {
    $bindings['DB_URL'] = 'jdbc:mysql://localhost:' + $dbPorts[$Service] + '/ticketya_' + $Service + '?allowPublicKeyRetrieval=true&useSSL=false'
    $bindings['DB_USER'] = 'ticketya'
    $bindings['DB_PASSWORD'] = $settings[$Service.ToUpperInvariant() + '_DB_PASSWORD']
    $bindings['SPRING_PROFILES_ACTIVE'] = 'default'
    if ($Service -eq 'eventos') { $bindings['SPRING_PROFILES_ACTIVE'] = 'mysql' }
    if ($Service -eq 'usuarios') {
        $bindings['SPRING_PROFILES_ACTIVE'] = 'dev'
        $bindings['JWT_PRIVATE_KEY'] = 'file:' + (Join-Path $repoRoot '.secrets/jwt-private.pem').Replace('\', '/')
        foreach ($name in @('SERVICE_EVENTOS_SECRET', 'SERVICE_RESERVAS_SECRET', 'SERVICE_COMPRAS_SECRET', 'SERVICE_ENTRADAS_SECRET',
            'BOOTSTRAP_ADMIN_ENABLED', 'BOOTSTRAP_ADMIN_EMAIL', 'BOOTSTRAP_ADMIN_PASSWORD')) {
            $bindings[$name] = $settings[$name]
        }
    } else {
        $bindings['SERVICE_CLIENT_SECRET'] = $settings['SERVICE_' + $Service.ToUpperInvariant() + '_SECRET']
        $bindings['SERVICE_TOKEN_URL'] = 'http://localhost:8085/auth/service-token'
    }
}
$previous = @{}
foreach ($key in $bindings.Keys) {
    $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
    [Environment]::SetEnvironmentVariable($key, $bindings[$key], 'Process')
}
Push-Location -LiteralPath (Join-Path $repoRoot $directories[$Service])
try {
    $mavenArgs = @('-B')
    if ($MavenRepository) { $mavenArgs += '-Dmaven.repo.local=' + $MavenRepository }
    $mavenArgs += 'spring-boot:run'
    & mvn @mavenArgs
    if ($LASTEXITCODE -ne 0) { throw "Maven finalizó con código $LASTEXITCODE" }
} finally {
    Pop-Location
    foreach ($key in $previous.Keys) { [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process') }
}
