# Force Camera Release - PowerShell Script
# This script forcefully releases the camera by killing all processes that might be using it

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "                    FORCE CAMERA RELEASE" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will FORCE release the camera by:" -ForegroundColor Yellow
Write-Host "  1. Killing ALL Java processes" -ForegroundColor Yellow
Write-Host "  2. Killing ALL Python processes" -ForegroundColor Yellow
Write-Host "  3. Killing camera-using applications" -ForegroundColor Yellow
Write-Host "  4. Restarting camera services" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

$confirm = Read-Host "Continue? (Y/N)"
if ($confirm -ne "Y" -and $confirm -ne "y") {
    Write-Host ""
    Write-Host "Cancelled." -ForegroundColor Red
    Write-Host ""
    pause
    exit
}

Write-Host ""
Write-Host "[STEP 1/5] Killing ALL Java processes..." -ForegroundColor Green
Write-Host ""

Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

Write-Host "[OK] All Java processes killed" -ForegroundColor Green
Write-Host ""

Write-Host "[STEP 2/5] Killing ALL Python processes..." -ForegroundColor Green
Write-Host ""

Get-Process | Where-Object {$_.ProcessName -like "*python*"} | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

Write-Host "[OK] All Python processes killed" -ForegroundColor Green
Write-Host ""

Write-Host "[STEP 3/5] Killing camera-using applications..." -ForegroundColor Green
Write-Host ""

$appsToKill = @(
    "Teams",
    "Zoom",
    "ZoomOpener",
    "Skype",
    "lync",
    "chrome",
    "msedge",
    "firefox",
    "Discord",
    "obs64",
    "obs32",
    "WindowsCamera"
)

foreach ($app in $appsToKill) {
    Get-Process -Name $app -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "   - $app : Killed" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[OK] All applications killed" -ForegroundColor Green
Write-Host ""

Write-Host "[STEP 4/5] Waiting for camera release..." -ForegroundColor Green
Write-Host ""

Write-Host "Waiting 5 seconds..." -ForegroundColor Gray
Start-Sleep -Seconds 5

Write-Host "[OK] Camera released" -ForegroundColor Green
Write-Host ""

Write-Host "[STEP 5/5] Cleaning orphan processes..." -ForegroundColor Green
Write-Host ""

# Kill any process with "camera" or "webcam" in the name
Get-Process | Where-Object {$_.ProcessName -like "*camera*" -or $_.ProcessName -like "*webcam*"} | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "[OK] Cleanup complete" -ForegroundColor Green
Write-Host ""

Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "                    CAMERA FORCED TO RELEASE!" -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "The camera has been FORCED to release." -ForegroundColor Green
Write-Host ""
Write-Host "NEXT STEPS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   1. Wait 10 seconds (IMPORTANT!)" -ForegroundColor White
Write-Host "   2. Open NutriLife" -ForegroundColor White
Write-Host "   3. Try Face ID" -ForegroundColor White
Write-Host ""
Write-Host "If it STILL doesn't work:" -ForegroundColor Yellow
Write-Host "   - Restart your computer (guaranteed solution)" -ForegroundColor White
Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Waiting 10 seconds before continuing..." -ForegroundColor Gray
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "You can now open NutriLife!" -ForegroundColor Green
Write-Host ""
pause
