param(
    [string]$RecursoId = "HIST-PAC-001"
)
Push-Location (Split-Path $PSScriptRoot -Parent)
mvn -q test "-Dtest=SeguridadAuditoriaYResidenciaTest#generaReporteReproducibleQuienVioDocumento"
Pop-Location
Get-Content (Join-Path (Split-Path $PSScriptRoot -Parent) "docs\reporte-auditoria-ejemplo.md")
