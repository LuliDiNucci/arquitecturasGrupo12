param(
    [string]$MavenRepository,
    [ValidateSet('usuarios', 'gateway', 'eventos', 'reservas', 'compras', 'entradas', 'todos')]
    [string]$Service = 'todos'
)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$modules = [ordered]@{
    usuarios = 'msvc-usuarios'; eventos = 'msvc-eventosylugares'; reservas = 'msvc-reservas'
    compras = 'msvc-compras'; entradas = 'msvc-entradas'; gateway = 'msvc-gateway'
}
$report = @()
foreach ($key in $modules.Keys) {
    if ($Service -ne 'todos' -and $Service -ne $key) { continue }
    $module = $modules[$key]
    Push-Location (Join-Path $repoRoot $module)
    try {
        foreach ($phase in @('compile', 'test', 'package')) {
            $mavenArgs = @('-B')
            if ($MavenRepository) { $mavenArgs += '-Dmaven.repo.local=' + $MavenRepository }
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
