@echo off
chcp 65001 >nul
title 瑶瑶纪念空间 - 后端服务

set JAR_FILE=backend\yaoyao-memorial-3.0.0.jar
set LOG_FILE=backend\app.log

echo ==========================================
echo   瑶瑶纪念空间 - 后端服务启动
echo ==========================================
echo.

if not exist "%JAR_FILE%" (
    echo [错误] 找不到JAR文件: %JAR_FILE%
    echo 请先执行构建: cd backend ^&^& mvn clean package -DskipTests
    pause
    exit /b 1
)

echo [信息] 正在启动后端服务...
echo [信息] 日志文件: %LOG_FILE%
echo [信息] 端口: 3000
echo [信息] 按 Ctrl+C 停止服务
echo.

java -jar "%JAR_FILE%" --spring.profiles.active=prod 2>&1 | tee "%LOG_FILE%"
