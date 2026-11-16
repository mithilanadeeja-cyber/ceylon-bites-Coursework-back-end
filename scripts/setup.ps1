$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (Test-Path '.env') { Write-Host '.env already exists. Keeping your configuration.'; exit 0 }
function New-Secret {
 $bytes = New-Object byte[] 32
 $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
 $rng.GetBytes($bytes)
 $rng.Dispose()
 return [Convert]::ToBase64String($bytes)
}
$db = New-Secret
$root = New-Secret
$jwt = New-Secret
$admin = New-Secret
@"
DB_PASSWORD=$db
MYSQL_ROOT_PASSWORD=$root
JWT_SECRET=$jwt
ADMIN_EMAIL=admin@ceylon.test
ADMIN_PASSWORD=$admin
"@ | Set-Content -Encoding ascii '.env'
Write-Host 'Created local configuration. Keep .env private and do not upload it.'
Write-Host 'Administrator email: admin@ceylon.test'
Write-Host 'Administrator password is the ADMIN_PASSWORD value inside .env.'
Write-Host 'Next: docker compose up --build -d'
Write-Host 'Backend health: http://localhost:8080/api/health. Start the frontend separately.'
