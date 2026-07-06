@echo off
chcp 65001 >nul
echo ================================
echo 同步原仓库更新到本地 fork
echo ================================

REM 获取最新的上游代码
echo.
echo [1/4] 正在从原仓库拉取最新代码...
git fetch upstream
if %errorlevel% neq 0 (
    echo 错误：拉取失败！
    pause
    exit /b 1
)

REM 同步 master-jdk17 分支
echo.
echo [2/4] 正在同步 master-jdk17 分支...
git checkout master-jdk17
git merge upstream/master-jdk17
git push origin master-jdk17

REM 同步 master 分支
echo.
echo [3/4] 正在同步 master 分支...
git checkout master
git merge upstream/master
git push origin master

REM 同步 develop 分支
echo.
echo [4/4] 正在同步 develop 分支...
git checkout develop
git merge upstream/develop
git push origin develop

echo.
echo ================================
echo ✅ 所有分支同步完成！
echo ================================
pause
