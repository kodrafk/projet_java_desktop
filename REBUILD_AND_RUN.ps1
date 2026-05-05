Write-Host "========================================" -ForegroundColor Green
Write-Host "Rebuilding NutriCoach Pro Application" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Step 1: Cleaning old build files..." -ForegroundColor Yellow
mvn clean

Write-Host ""
Write-Host "Step 2: Compiling source code..." -ForegroundColor Yellow
mvn compile

Write-Host ""
Write-Host "Step 3: Running the application..." -ForegroundColor Yellow
mvn javafx:run

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
