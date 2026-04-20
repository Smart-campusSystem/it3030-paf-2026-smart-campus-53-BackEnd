# Starts the API with env vars from scripts\local-secrets.ps1 (gitignored).
# Setup once: copy scripts\local-secrets.ps1.example to scripts\local-secrets.ps1 and edit.

$ErrorActionPreference = 'Stop'
$BackendRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$secretsPath = Join-Path $PSScriptRoot 'local-secrets.ps1'

if (-not (Test-Path $secretsPath)) {
	Write-Host "Missing: $secretsPath" -ForegroundColor Red
	Write-Host "Copy scripts\local-secrets.ps1.example -> scripts\local-secrets.ps1 and set MYSQL_PASSWORD to match MySQL Workbench (case-sensitive; ...@Hk vs ...@HK is a common mistake)." -ForegroundColor Yellow
	exit 1
}

Set-Location $BackendRoot
. $secretsPath

Write-Host "Starting Spring Boot with a clean compile (avoids stale Ticket/entity bytecode)..." -ForegroundColor Cyan
& .\mvnw.cmd @('clean', 'spring-boot:run')
