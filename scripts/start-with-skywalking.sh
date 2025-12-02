#!/bin/bash
#
# 使用 SkyWalking Agent 启动 yudao-server
#
# 使用方式：
#   ./scripts/start-with-skywalking.sh [profile]
#
# 参数：
#   profile - Spring Profile，默认为 local
#
# 前置条件：
#   1. 下载并解压 SkyWalking Agent 到 skywalking-agent 目录
#   2. SkyWalking OAP Server 已启动（docker/skywalking）

set -e

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

# 配置参数
PROFILE="${1:-local}"
SERVICE_NAME="yudao-server"
SW_BACKEND="${SW_BACKEND:-127.0.0.1:11800}"
JAR_FILE="yudao-server/target/yudao-server.jar"
LOG_FILE="/tmp/yudao-server-skywalking.log"

# SkyWalking Agent 路径
SW_AGENT_PATH="${SW_AGENT_PATH:-$PROJECT_ROOT/skywalking-agent/skywalking-agent.jar}"

# 检查 JAR 文件
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: $JAR_FILE not found!"
    echo "Please run: mvn clean install -DskipTests"
    exit 1
fi

# 检查 SkyWalking Agent
if [ ! -f "$SW_AGENT_PATH" ]; then
    echo "=================================================="
    echo "SkyWalking Agent not found at: $SW_AGENT_PATH"
    echo ""
    echo "Please download and extract the agent:"
    echo ""
    echo "  cd $PROJECT_ROOT"
    echo "  wget https://archive.apache.org/dist/skywalking/java-agent/9.1.0/apache-skywalking-java-agent-9.1.0.tgz"
    echo "  tar -xzf apache-skywalking-java-agent-9.1.0.tgz"
    echo "  mv skywalking-agent skywalking-agent"
    echo ""
    echo "Or set SW_AGENT_PATH environment variable to your agent location."
    echo "=================================================="
    exit 1
fi

# 检查 SkyWalking OAP Server
echo "Checking SkyWalking OAP Server at $SW_BACKEND..."
OAP_HOST=$(echo $SW_BACKEND | cut -d: -f1)
OAP_PORT=$(echo $SW_BACKEND | cut -d: -f2)

if ! nc -z "$OAP_HOST" "$OAP_PORT" 2>/dev/null; then
    echo "Warning: SkyWalking OAP Server not reachable at $SW_BACKEND"
    echo "Please start it with: cd docker/skywalking && docker-compose up -d"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 停止已有进程
echo "Stopping existing yudao-server process..."
pkill -f "yudao-server.jar" 2>/dev/null || true
sleep 2

# 启动应用
echo "=================================================="
echo "Starting $SERVICE_NAME with SkyWalking Agent"
echo "  Profile: $PROFILE"
echo "  Agent: $SW_AGENT_PATH"
echo "  Backend: $SW_BACKEND"
echo "  Log: $LOG_FILE"
echo "=================================================="

nohup java \
    -javaagent:"$SW_AGENT_PATH" \
    -Dskywalking.agent.service_name="$SERVICE_NAME" \
    -Dskywalking.collector.backend_service="$SW_BACKEND" \
    -Dskywalking.agent.instance_name="$(hostname)-$$" \
    -Dskywalking.logging.level=INFO \
    -jar "$JAR_FILE" \
    --spring.profiles.active="$PROFILE" \
    > "$LOG_FILE" 2>&1 &

PID=$!
echo "Started with PID: $PID"
echo ""
echo "Waiting for application to start..."

# 等待启动
for i in {1..60}; do
    if curl -s http://localhost:48080/actuator/health > /dev/null 2>&1; then
        echo ""
        echo "=================================================="
        echo "Application started successfully!"
        echo ""
        echo "  Backend API: http://localhost:48080"
        echo "  SkyWalking UI: http://localhost:8080"
        echo "  Log file: $LOG_FILE"
        echo ""
        echo "Make some requests and check SkyWalking UI for traces!"
        echo "=================================================="
        exit 0
    fi
    echo -n "."
    sleep 2
done

echo ""
echo "Timeout waiting for application to start."
echo "Check log file: $LOG_FILE"
tail -50 "$LOG_FILE"
exit 1
