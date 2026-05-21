@echo off
title ruoyi-vue-pro Stopper

echo ========================================
echo  ruoyi-vue-pro Stop
echo ========================================

echo [1/2] Stopping backend ...
wmic process where "name='java.exe' and commandline like '%%yudao-server%%'" delete >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq YudaoServer" >nul 2>&1
echo Backend stopped

echo [2/2] Stopping frontend ...
wmic process where "name='node.exe' and commandline like '%%vite%%'" delete >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq YudaoUI" >nul 2>&1
echo Frontend stopped

echo.
echo ========================================
echo  All services stopped
echo ========================================
echo.
if not "%1"=="nopause" pause
