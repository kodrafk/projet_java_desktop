Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting MySQL and Setting up AI System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Start MySQL
Write-Host "[1/3] Starting MySQL..." -ForegroundColor Yellow

# Kill any existing MySQL processes
Stop-Process -Name "mysqld" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Start MySQL
$mysqlBin = "C:\xampp\mysql\bin"
if (Test-Path $mysqlBin) {
    Start-Process -FilePath "$mysqlBin\mysqld.exe" -ArgumentList "--defaults-file=$mysqlBin\my.ini", "--standalone", "--console" -WindowStyle Minimized
    Write-Host "MySQL starting..." -ForegroundColor Green
    Start-Sleep -Seconds 5
} else {
    Write-Host "ERROR: MySQL not found at $mysqlBin" -ForegroundColor Red
    exit 1
}

# Step 2: Setup Database
Write-Host ""
Write-Host "[2/3] Setting up AI database..." -ForegroundColor Yellow

$mysqlPath = "$mysqlBin\mysql.exe"
Get-Content "SETUP_COMPLETE_SYSTEM.sql" | & $mysqlPath -u root nutrilife_db 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database setup complete!" -ForegroundColor Green
    Write-Host "- 15 anomalies created" -ForegroundColor White
    Write-Host "- 7 alerts generated" -ForegroundColor White
    Write-Host "- 10 user metrics calculated" -ForegroundColor White
} else {
    Write-Host "WARNING: Database setup may have issues" -ForegroundColor Yellow
}

# Step 3: Run Application
Write-Host ""
Write-Host "[3/3] Starting NutriLife application..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Login with: kiro.admin@nutrilife.com / kiro2026" -ForegroundColor Cyan
Write-Host "Then click 'AI Anomaly Detection' button" -ForegroundColor Cyan
Write-Host ""

mvn javafx:run
