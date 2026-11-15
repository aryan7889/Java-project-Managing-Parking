@echo off
setlocal
echo ==================================================
echo   Building Smart Parking Management System...
echo ==================================================

if not exist bin mkdir bin
if not exist data mkdir data

javac -d bin src\com\smartparking\*.java src\com\smartparking\model\*.java src\com\smartparking\exception\*.java src\com\smartparking\service\*.java src\com\smartparking\util\*.java src\com\smartparking\web\*.java src\com\smartparking\test\*.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Please ensure JDK 8+ is installed and on your PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo [OK] Compilation successful!
echo.
echo Launching Application...
java -cp bin com.smartparking.ParkingManager

pause
