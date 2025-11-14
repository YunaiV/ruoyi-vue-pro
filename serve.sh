#!/bin/bash

# 芋道快速开发平台 - 启动脚本
# 用法:
#   ./serve.sh --service    # 启动后端服务
#   ./serve.sh --web        # 启动前端服务(Vben版本)
#   ./serve.sh --web-mall   # 启动商城前端(UniApp)
#   ./serve.sh --all        # 同时启动前后端
#   ./serve.sh --help       # 显示帮助信息

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${PROJECT_ROOT}/yudao-server"
FRONTEND_VBEN_DIR="${PROJECT_ROOT}/yudao-ui/yudao-ui-admin-vben"
FRONTEND_MALL_DIR="${PROJECT_ROOT}/yudao-ui/yudao-ui-mall-uniapp"

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_title() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# 检查依赖服务
check_dependencies() {
    log_title "检查依赖服务"

    # 检查 Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    # 检查 MySQL
    log_info "检查 MySQL 容器..."
    if ! docker ps --format '{{.Names}}' | grep -q "mysql"; then
        log_warn "MySQL 容器未运行，请确保 MySQL 服务可用"
    else
        log_info "✓ MySQL 容器运行中"
    fi

    # 检查 Redis
    log_info "检查 Redis 容器..."
    if ! docker ps --format '{{.Names}}' | grep -q "redis"; then
        log_warn "Redis 容器未运行，请确保 Redis 服务可用"
    else
        log_info "✓ Redis 容器运行中"
    fi

    echo ""
}

# 编译项目
build_project() {
    log_title "编译项目"
    cd "${PROJECT_ROOT}"

    log_info "正在编译项目 (使用 4 个线程)..."
    log_info "这可能需要几分钟，请耐心等待..."
    mvn clean install -DskipTests -T 4

    if [ $? -ne 0 ]; then
        log_error "编译失败，请检查错误信息"
        exit 1
    fi

    log_info "✓ 编译完成"
    echo ""
}

# 启动后端服务
start_backend() {
    log_title "启动后端服务"

    # 检查是否已编译
    if [ ! -d "${BACKEND_DIR}/target" ]; then
        log_info "检测到项目未编译，开始编译..."
        build_project
    fi

    cd "${BACKEND_DIR}"

    log_info "启动 Spring Boot 应用..."
    log_info "访问地址: http://localhost:48080"
    log_info "API 文档: http://localhost:48080/doc.html"
    log_info "Swagger UI: http://localhost:48080/swagger-ui.html"
    echo ""

    # 启动服务
    mvn spring-boot:run
}

# 启动前端服务(Vben版本)
start_frontend_vben() {
    log_title "启动前端服务 (Vben Admin)"

    cd "${FRONTEND_VBEN_DIR}"

    # 检查是否已安装依赖
    if [ ! -d "node_modules" ]; then
        log_info "首次启动，正在安装依赖..."
        if command -v pnpm &> /dev/null; then
            log_info "使用 pnpm 安装依赖..."
            pnpm install
        elif command -v yarn &> /dev/null; then
            log_info "使用 yarn 安装依赖..."
            yarn install
        else
            log_info "使用 npm 安装依赖..."
            npm install
        fi
    fi

    log_info "启动开发服务器..."
    log_info "前端地址将在启动后显示"
    echo ""

    # 启动服务
    if command -v pnpm &> /dev/null; then
        pnpm run dev
    elif command -v yarn &> /dev/null; then
        yarn dev
    else
        npm run dev
    fi
}

# 启动商城前端(UniApp)
start_frontend_mall() {
    log_title "启动商城前端 (UniApp)"

    cd "${FRONTEND_MALL_DIR}"

    # 检查是否已安装依赖
    if [ ! -d "node_modules" ]; then
        log_info "首次启动，正在安装依赖..."
        if command -v pnpm &> /dev/null; then
            pnpm install
        elif command -v yarn &> /dev/null; then
            yarn install
        else
            npm install
        fi
    fi

    log_info "启动 UniApp 开发服务器..."
    echo ""

    # 启动服务
    if command -v pnpm &> /dev/null; then
        pnpm run dev:h5
    elif command -v yarn &> /dev/null; then
        yarn dev:h5
    else
        npm run dev:h5
    fi
}

# 同时启动前后端
start_all() {
    log_title "同时启动前后端服务"
    log_warn "将在后台启动后端，前台启动前端"
    echo ""

    # 后台启动后端
    log_info "正在后台启动后端服务..."
    cd "${BACKEND_DIR}"
    nohup mvn spring-boot:run > "${PROJECT_ROOT}/backend.log" 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > "${PROJECT_ROOT}/.backend.pid"
    log_info "后端 PID: ${BACKEND_PID}"
    log_info "后端日志: ${PROJECT_ROOT}/backend.log"

    # 等待后端启动
    log_info "等待后端启动 (30秒)..."
    sleep 30

    # 前台启动前端
    start_frontend_vben
}

# 显示帮助信息
show_help() {
    cat << EOF
芋道快速开发平台 - 启动脚本

用法:
    ./serve.sh [选项]

选项:
    --build         编译整个项目 (首次运行必须执行)
    --service       启动后端服务 (Spring Boot)
    --web           启动前端服务 (Vben Admin - 管理后台)
    --web-mall      启动商城前端 (UniApp)
    --all           同时启动前后端 (后端后台运行)
    --help          显示此帮助信息

示例:
    ./serve.sh --build        # 首次使用，编译项目
    ./serve.sh --service      # 仅启动后端
    ./serve.sh --web          # 仅启动前端
    ./serve.sh --all          # 启动全部

服务地址:
    后端 API:   http://localhost:48080
    API 文档:   http://localhost:48080/doc.html
    前端地址:   启动后显示 (通常是 http://localhost:5173)

依赖服务:
    - MySQL:    localhost:3306  (数据库: ruoyi-vue-pro)
    - Redis:    127.0.0.1:6379  (database: 6)
    - Qdrant:   localhost:6334  (可选，AI 功能需要)

停止服务:
    - 前端: Ctrl+C
    - 后端: 查找并停止 Java 进程
      ps aux | grep yudao-server
      kill -9 <PID>

EOF
}

# 主逻辑
main() {
    if [ $# -eq 0 ]; then
        log_error "缺少参数，请使用 --help 查看帮助"
        exit 1
    fi

    case "$1" in
        --build)
            build_project
            ;;
        --service)
            check_dependencies
            start_backend
            ;;
        --web)
            start_frontend_vben
            ;;
        --web-mall)
            start_frontend_mall
            ;;
        --all)
            check_dependencies
            start_all
            ;;
        --help|-h)
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            echo "使用 --help 查看帮助"
            exit 1
            ;;
    esac
}

main "$@"
