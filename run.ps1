Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Building Smart Parking Management System...   " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

if (-not (Test-Path bin)) { New-Item -ItemType Directory -Path bin | Out-Null }
if (-not (Test-Path data)) { New-Item -ItemType Directory -Path data | Out-Null }

javac -d bin src/com/smartparking/*.java src/com/smartparking/model/*.java src/com/smartparking/exception/*.java src/com/smartparking/service/*.java src/com/smartparking/util/*.java src/com/smartparking/web/*.java src/com/smartparking/test/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Compilation failed! Please check JDK installation." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[OK] Compilation successful!`n" -ForegroundColor Green
Write-Host "Launching Application..." -ForegroundColor Yellow
java -cp bin com.smartparking.ParkingManager
