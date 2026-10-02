@echo off
setlocal
cd /d "%~dp0\.."

echo ==========================================
echo  Launching The Lost Facility (JavaFX GUI) 
echo ==========================================

if exist "mvnw.cmd" (
    call .\mvnw.cmd javafx:run %*
) else (
    call mvn javafx:run %*
)
pause
