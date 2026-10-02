@echo off
echo Downloading and installing project dependencies...
.\mvnw.cmd dependency:go-offline clean compile
echo.
echo Setup Complete! All dependencies have been successfully installed.
pause