param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$')]
    [string]$CorrelationId,
    [string]$LokiUrl = 'http://localhost:3100',
    [int]$Minutes = 30
)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$end = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() * [long]1000000
$start = [DateTimeOffset]::UtcNow.AddMinutes(-$Minutes).ToUnixTimeMilliseconds() * [long]1000000
$query = '{job="ticketya"} | json | correlationId="' + $CorrelationId + '"'
$uri = $LokiUrl.TrimEnd('/') + '/loki/api/v1/query_range?query=' + [uri]::EscapeDataString($query) + '&start=' + $start + '&end=' + $end + '&limit=2000&direction=forward'
$found = @()
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    $response = Invoke-RestMethod -Uri $uri -TimeoutSec 10
    if ($response.status -ne 'success') { throw 'Loki no devolvió una consulta exitosa' }
    $found = @($response.data.result | ForEach-Object { $_.stream.servicio } | Where-Object { $_ } | Sort-Object -Unique)
    if ($found.Count -ge 2) { break }
    Start-Sleep -Seconds 1
}
if ($found.Count -lt 2) { throw ('No se encontraron dos microservicios para ' + $CorrelationId + '. No se considera pasada la prueba.') }
$reportDir = Join-Path $root '.local-observability'
$null = New-Item -ItemType Directory -Path $reportDir -Force
$report = @{ result = 'PASS'; correlationId = $CorrelationId; services = $found; query = $query; response = $response }
$path = Join-Path $reportDir ('loki-' + $CorrelationId + '.json')
$report | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $path -Encoding UTF8
Write-Output ('PASS: ' + ($found -join ', ') + '. Evidencia: ' + $path)
