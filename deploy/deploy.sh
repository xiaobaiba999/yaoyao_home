#!/bin/bash

set -e

DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_FILE="$DEPLOY_DIR/deploy.log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "  瑶瑶纪念空间 - 自动部署"
log "=========================================="

log "[1/6] 检查环境依赖..."
if ! command -v java &> /dev/null; then
    log "错误: 未安装 Java 17+"
    exit 1
fi
if ! command -v mysql &> /dev/null; then
    log "警告: 未找到 mysql 客户端，跳过数据库初始化"
fi
log "Java: $(java -version 2>&1 | head -1)"

log "[2/6] 检查数据库..."
if command -v mysql &> /dev/null; then
    DB_EXISTS=$(mysql -u root -p"${DB_PASSWORD:-}" -e "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='yaoyao_memorial';" -s -N 2>/dev/null || echo "0")
    if [ "$DB_EXISTS" = "0" ]; then
        log "数据库不存在，执行初始化..."
        bash "$DEPLOY_DIR/scripts/init-db.sh"
    else
        log "数据库已存在，跳过初始化"
    fi
fi

log "[3/6] 停止旧的后端服务..."
bash "$DEPLOY_DIR/scripts/backend.sh" stop 2>/dev/null || true

log "[4/6] 启动新的后端服务..."
bash "$DEPLOY_DIR/scripts/backend.sh" start

log "[5/6] 重载 Nginx..."
if command -v nginx &> /dev/null; then
    nginx -t 2>&1 | tee -a "$LOG_FILE"
    nginx -s reload
    log "Nginx 重载成功"
else
    log "警告: 未找到 nginx，跳过重载"
fi

log "[6/6] 健康检查..."
sleep 5

BACKEND_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/health 2>/dev/null || echo "000")
if [ "$BACKEND_HEALTH" = "200" ]; then
    log "后端健康检查通过 (HTTP 200)"
else
    log "后端健康检查失败 (HTTP $BACKEND_HEALTH)"
fi

FRONTEND_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/ 2>/dev/null || echo "000")
if [ "$FRONTEND_HEALTH" = "200" ]; then
    log "前端健康检查通过 (HTTP 200)"
else
    log "前端健康检查失败 (HTTP $FRONTEND_HEALTH)"
fi

log "=========================================="
log "  部署完成！"
log "  前端: http://localhost/"
log "  后端: http://localhost:3000/api/health"
log "=========================================="
