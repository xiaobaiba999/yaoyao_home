@echo off
chcp 65001 >nul
title 瑶瑶纪念空间 - 数据库初始化

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=yaoyao_memorial

echo ==========================================
echo   瑶瑶纪念空间 - 数据库初始化
echo ==========================================
echo.

set /p DB_USERNAME="请输入MySQL用户名 (默认root): "
if "%DB_USERNAME%"=="" set DB_USERNAME=root

set /p DB_PASSWORD="请输入MySQL密码: "

echo.
echo [1/3] 创建数据库...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo [错误] 创建数据库失败，请检查MySQL连接信息
    pause
    exit /b 1
)
echo 数据库 %DB_NAME% 创建成功

echo [2/3] 初始化表结构...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% %DB_NAME% < sql\schema.sql 2>nul
if %errorlevel% neq 0 (
    echo [错误] 初始化表结构失败
    pause
    exit /b 1
)
echo 表结构初始化成功

echo [3/3] 验证表...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% %DB_NAME% -e "SHOW TABLES;" 2>nul

echo.
echo 数据库初始化完成！
pause
