@echo off
setlocal
cd /d "%~dp0\.."

echo ==========================================
echo  Launching The Lost Facility (Terminal CLI)
echo ==========================================

if exist "mvnw.cmd" (
    call .\mvnw.cmd exec:java -Dexec.mainClass="lostfacility.cli.CliApp" %*
) else (
    call mvn exec:java -Dexec.mainClass="lostfacility.cli.CliApp" %*
)
pause
