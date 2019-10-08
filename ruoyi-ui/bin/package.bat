@echo off
echo.
echo [信息] 打包Web工程，生成node_modules包文件。
echo.

%~d0
cd %~dp0

cd ..
npm install --registry=https://registry.npm.taobao.org

pause