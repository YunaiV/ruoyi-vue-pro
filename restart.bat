@echo off
title ruoyi-vue-pro Restarter

echo ========================================
echo  ruoyi-vue-pro Restart
echo ========================================

echo Stopping services ...
call "%~dp0stop.bat" nopause

echo Waiting 3 seconds ...
timeout /t 3 /nobreak >nul

echo Starting services ...
call "%~dp0start.bat"

exit /b
