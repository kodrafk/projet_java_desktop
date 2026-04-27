Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI Anomaly Detection System Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find MySQL
$mysqlPath = $null
$possiblePaths = @(
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $mysqlPath = $path
        break
    }
}

if (-not $mysqlPath) {
    Write-Host "ERROR: MySQL not found!" -ForegroundColor Red
    exit 1
}

Write-Host "Using MySQL: $mysqlPath" -ForegroundColor Green
Write-Host ""
Write-Host "Creating tables and inserting sample data..." -ForegroundColor Yellow

# Execute SQL
Get-Content "SETUP_COMPLETE_SYSTEM.sql" | & $mysqlPath -u root nutrilife_db

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "SUCCESS! AI System is ready!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "- 15 sample anomalies created" -ForegroundColor White
    Write-Host "- 7 alerts generated" -ForegroundColor White
    Write-Host "- 10 user metrics calculated" -ForegroundColor White
    Write-Host ""
    Write-Host "Starting application..." -ForegroundColor Cyan
    Write-Host ""
    
    # Run the application
    mvn javafx:run
} else {
    Write-Host ""
    Write-Host "ERROR: Setup failed!" -ForegroundColor Red
    Write-Host "Check if MySQL is running." -ForegroundColor Yellow
}
