#!/bin/bash

DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$DEPLOY_DIR/backend/yaoyao-memorial-3.0.0.jar"
LOG_FILE="$DEPLOY_DIR/backend/app.log"
PID_FILE="$DEPLOY_DIR/backend/app.pid"

start() {
    if [ -f "$PID_FILE" ]; then
        OLD_PID=$(cat "$PID_FILE")
        if kill -0 "$OLD_PID" 2>/dev/null; then
            echo "后端服务已在运行中 (PID: $OLD_PID)"
            return 1
        fi
        rm -f "$PID_FILE"
    fi

    if [ ! -f "$JAR_FILE" ]; then
        echo "错误: 找不到JAR文件: $JAR_FILE"
        return 1
    fi

    echo "正在启动后端服务..."
    nohup java -jar "$JAR_FILE" \
        --spring.profiles.active=prod \
        > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"

    sleep 3

    if kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        echo "后端服务启动成功 (PID: $(cat "$PID_FILE"))"
        echo "日志文件: $LOG_FILE"
    else
        echo "后端服务启动失败，请检查日志: $LOG_FILE"
        rm -f "$PID_FILE"
        return 1
    fi
}

stop() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo "正在停止后端服务 (PID: $PID)..."
            kill "$PID"
            sleep 3
            if kill -0 "$PID" 2>/dev/null; then
                kill -9 "$PID"
            fi
            echo "后端服务已停止"
        else
            echo "后端服务未在运行"
        fi
        rm -f "$PID_FILE"
    else
        echo "PID文件不存在，后端服务可能未在运行"
    fi
}

status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo "后端服务运行中 (PID: $PID)"
            HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/health 2>/dev/null)
            echo "健康检查: HTTP $HEALTH"
        else
            echo "后端服务未在运行 (PID文件过期)"
        fi
    else
        echo "后端服务未在运行"
    fi
}

case "$1" in
    start)   start ;;
    stop)    stop ;;
    restart) stop; sleep 2; start ;;
    status)  status ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
