@echo off
title ruoyi-vue-pro Launcher
setlocal enabledelayedexpansion

set "PRJ=D:\opencodeproject"
set "JAVA_HOME=D:\Program Files\Java\jdk-21.0.11"
set "NODE_HOME=D:\Program Files\nodejs"
set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
set "NODE_BIN=%NODE_HOME%\node.exe"
set "JAR=%PRJ%\ruoyi-vue-pro\yudao-server\target\yudao-server.jar"
set "VITE=%PRJ%\yudao-ui-admin-vue3\node_modules\vite\bin\vite.js"
set "JAVA_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED"
set "START_LOG=%TEMP%\yudao-server-start.log"

echo ========================================
echo  ruoyi-vue-pro Start
echo ========================================

if not exist "%JAR%" (
    echo ERROR: Backend jar not found at "%JAR%"
    echo Run mvn clean install package -Dmaven.test.skip=true first
    pause
    exit /b 1
)

echo [1/3] Stopping any old instances ...
wmic process where "name='java.exe' and commandline like '%%yudao-server%%'" delete >nul 2>&1
wmic process where "name='node.exe' and commandline like '%%vite%%'" delete >nul 2>&1
timeout /t 2 /nobreak >nul
echo Done

echo [2/3] Starting Backend (port 48080) ...
start "YudaoServer" "%JAVA_BIN%" %JAVA_OPTS% -jar "%JAR%" --spring.profiles.active=local

set "RETRIES=0"
:wait_loop
set /a RETRIES+=1
if !RETRIES! gtr 30 (
    echo ERROR: Backend failed to start within 60 seconds
    echo Check "%START_LOG%" for details
    pause
    exit /b 1
)
ping -n 3 127.0.0.1 >nul
curl -s http://127.0.0.1:48080 >nul 2>&1
if errorlevel 1 goto wait_loop
echo Backend OK

echo [3/3] Starting Frontend (port 80) ...
start "YudaoUI" cmd /c "cd /d "%PRJ%\yudao-ui-admin-vue3" && "%NODE_BIN%" "node_modules\vite\bin\vite.js" --mode env.local --host 0.0.0.0"
timeout /t 8 /nobreak >nul

curl -s http://127.0.0.1 >nul 2>&1
if errorlevel 1 (
    echo WARNING: Frontend may not be ready yet
    echo Check the "YudaoUI" window for errors
    echo If port 80 is blocked, edit .env and change VITE_PORT
)

echo.
echo ========================================
echo  All services started
echo  Backend:  http://127.0.0.1:48080
echo  Frontend: http://127.0.0.1
echo  Login:    admin / admin123
echo ========================================
echo.
pause
