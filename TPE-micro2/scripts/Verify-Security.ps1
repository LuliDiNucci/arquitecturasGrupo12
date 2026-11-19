param(
    [string]$MavenRepository,
    [ValidateSet('observability', 'usuarios', 'gateway', 'eventos', 'reservas', 'compras', 'entradas', 'notificaciones', 'fila-virtual', 'recomendaciones', 'todos')]
    [string]$Service = 'todos',
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$modules = [ordered]@{
    observability = 'observability'; usuarios = 'msvc-usuarios'; eventos = 'msvc-eventosylugares'; reservas = 'msvc-reservas'
    compras = 'msvc-compras'; entradas = 'msvc-entradas'; gateway = 'msvc-gateway'
    notificaciones = 'msvc-notificaciones'; 'fila-virtual' = 'msvc-fila-virtual'; recomendaciones = 'msvc-recomendaciones'
}
$report = @()
foreach ($key in $modules.Keys) {
    if ($Service -ne 'todos' -and $Service -ne $key -and $key -ne 'observability') { continue }
    $module = $modules[$key]
    Push-Location (Join-Path $repoRoot $module)
    try {
        # install incluye compile, test, package y verify, con una sola ejecución de tests.
        # La biblioteca se instala primero para mantener builds independientes.
        foreach ($phase in @('install')) {
            $mavenArgs = @('-B')
            if ($MavenRepository) { $mavenArgs += '-Dmaven.repo.local=' + $MavenRepository }
            if ($Offline) { $mavenArgs += '-o' }
            $mavenArgs += @('clean', $phase)
            # En Windows PowerShell 5, un warning de stderr no debe convertirse
            # en una excepción que interrumpa un proceso Maven exitoso.
            $previousErrorPreference = $ErrorActionPreference
            $ErrorActionPreference = 'Continue'
            try {
                & mvn @mavenArgs *> ('build-' + $phase + '.log')
                $code = $LASTEXITCODE
            } finally { $ErrorActionPreference = $previousErrorPreference }
            $report += [pscustomobject]@{ service = $module; command = 'mvn clean ' + $phase; exitCode = $code }
            $report | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $repoRoot ('verification-' + $Service + '.json')) -Encoding UTF8
            Write-Output ($module + ' / ' + $phase + ' / exit=' + $code)
            if ($code -ne 0) { throw ('Falló ' + $module + ': consultar build-' + $phase + '.log') }
        }
    } finally { Pop-Location }
}
