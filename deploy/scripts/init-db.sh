#!/bin/bash

MYSQL_USER="${DB_USERNAME:-root}"
MYSQL_PASSWORD="${DB_PASSWORD:-}"
MYSQL_HOST="${DB_HOST:-localhost}"
MYSQL_PORT="${DB_PORT:-3306}"
DB_NAME="yaoyao_memorial"

echo "=========================================="
echo "  瑶瑶纪念空间 - 数据库初始化"
echo "=========================================="
echo ""

if [ -z "$MYSQL_PASSWORD" ]; then
    echo "请输入MySQL密码:"
    read -s MYSQL_PASSWORD
fi

echo "[1/3] 创建数据库..."
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "
    CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "创建数据库失败，请检查MySQL连接信息"
    exit 1
fi
echo "数据库 $DB_NAME 创建成功"

echo "[2/3] 初始化表结构..."
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$DB_NAME" < "$SCRIPT_DIR/../sql/schema.sql" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "初始化表结构失败"
    exit 1
fi
echo "表结构初始化成功"

echo "[3/3] 验证表..."
TABLE_COUNT=$(mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null | wc -l)
echo "已创建 $((TABLE_COUNT - 1)) 个表"

echo ""
echo "数据库初始化完成！"
