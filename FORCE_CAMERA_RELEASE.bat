@echo off
cd /d "%~dp0"
color 0E
title Force Camera Release

echo.
echo ========================================================================
echo                    FORCE CAMERA RELEASE
echo ========================================================================
echo.
echo Execution du script PowerShell pour forcer la liberation de la camera...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0FORCE_CAMERA_RELEASE.ps1"

echo.
echo Script termine.
echo.
pause
