@echo off
echo.
echo [信息] 安装Web工程，生成node_modules文件。
echo.

%~d0
cd %~dp0

cd ..
npm install --registry=https://registry.npm.taobao.org

pause