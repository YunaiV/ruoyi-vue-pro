#!/bin/bash
set -e

# ============================================
# 本地开发环境一键部署脚本
# 用于企业级 CI/CD 流程学习
# ============================================

echo "========================================"
echo "🚀 芋道项目本地开发环境部署"
echo "========================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 Docker 是否运行
check_docker() {
    echo "📦 检查 Docker 环境..."
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker 未运行，请先启动 Docker Desktop 或 OrbStack${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Docker 环境正常${NC}"
    echo ""
}

# 检查端口占用
check_ports() {
    echo "🔍 检查端口占用..."
    local ports=(3306 6379 48080 80 8080)
    local port_used=0
    
    for port in "${ports[@]}"; do
        if lsof -Pi :$port -sTCP:LISTEN -t > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠️  端口 $port 已被占用${NC}"
            port_used=1
        fi
    done
    
    if [ $port_used -eq 1 ]; then
        echo ""
        echo -e "${YELLOW}提示：如果端口冲突，请修改 docker-compose.full.yml 中的端口映射${NC}"
        echo ""
    else
        echo -e "${GREEN}✅ 所有端口可用${NC}"
        echo ""
    fi
}

# 启动基础服务（MySQL + Redis）
start_base_services() {
    echo "🗄️  启动基础服务（MySQL + Redis）..."
    docker-compose -f docker-compose.full.yml up -d mysql redis
    
    echo "等待数据库就绪..."
    for i in $(seq 1 30); do
        if docker exec ruoyi-mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
            echo -e "${GREEN}✅ MySQL 已就绪${NC}"
            break
        fi
        echo -n "."
        sleep 2
    done
    echo ""
}

# 构建并启动后端
start_backend() {
    echo "🔨 构建后端服务..."
    docker-compose -f docker-compose.full.yml build backend
    
    echo "🚀 启动后端服务..."
    docker-compose -f docker-compose.full.yml up -d backend
    
    echo "等待后端服务启动..."
    for i in $(seq 1 60); do
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:48080/actuator/health 2>/dev/null || echo "000")
        if [ "$RESPONSE" = "200" ]; then
            echo -e "${GREEN}✅ 后端服务已就绪${NC}"
            break
        fi
        echo -n "."
        sleep 3
    done
    echo ""
}

# 构建并启动前端
start_frontend() {
    echo "🎨 构建前端服务..."
    docker-compose -f docker-compose.full.yml build frontend
    
    echo "🚀 启动前端服务..."
    docker-compose -f docker-compose.full.yml up -d frontend
    echo -e "${GREEN}✅ 前端服务已启动${NC}"
    echo ""
}

# 启动 Jenkins
start_jenkins() {
    echo "⚙️  启动 Jenkins CI/CD..."
    docker-compose -f docker-compose.full.yml up -d jenkins
    echo -e "${YELLOW}⏳ Jenkins 首次启动需要 1-2 分钟初始化${NC}"
    echo ""
}

# 显示部署信息
show_info() {
    echo "========================================"
    echo -e "${GREEN}✅ 部署完成！${NC}"
    echo "========================================"
    echo ""
    echo "📊 服务访问地址："
    echo "  🌐 前端页面:    http://localhost"
    echo "  🔧 后端 API:    http://localhost:48080"
    echo "  ⚙️  Jenkins:    http://localhost:8080"
    echo "  📊 Druid监控:   http://localhost:48080/druid"
    echo ""
    echo "💾 数据库连接："
    echo "  🗄️  MySQL:      localhost:3306 (root/root123456)"
    echo "  🔴 Redis:       localhost:6379"
    echo ""
    echo "📝 常用命令："
    echo "  查看状态:   docker-compose -f docker-compose.full.yml ps"
    echo "  查看日志:   docker-compose -f docker-compose.full.yml logs -f"
    echo "  停止服务:   docker-compose -f docker-compose.full.yml down"
    echo "  重启服务:   docker-compose -f docker-compose.full.yml restart"
    echo ""
    echo "🎯 Jenkins 首次配置："
    echo "  1. 访问 http://localhost:8080"
    echo "  2. 获取初始密码: docker exec ruoyi-jenkins cat /var/jenkins_home/secrets/initialAdminPassword"
    echo "  3. 选择安装推荐插件"
    echo "  4. 创建管理员账号"
    echo "  5. 配置流水线（使用 Jenkinsfile-local）"
    echo ""
    echo "========================================"
}

# 主流程
main() {
    check_docker
    check_ports
    
    echo "开始部署服务..."
    echo ""
    
    start_base_services
    start_backend
    start_frontend
    start_jenkins
    
    show_info
}

# 执行主流程
main
