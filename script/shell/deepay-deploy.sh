#!/bin/bash
# =============================================================
#  Deepay 一键自动部署脚本
#
#  用法（服务器上执行）：
#    cd /www/wwwroot/deepay.srl
#    bash script/shell/deepay-deploy.sh [backend|frontend|all]
#
#  不带参数默认 all（后端 + 前端全部部署）
# =============================================================
set -euo pipefail

MODE=${1:-all}   # all | backend | frontend

# ============================================================
# ★ 所有路径 / 参数集中在这里，按需修改
# ============================================================
PROJECT_ROOT=/www/wwwroot/deepay.srl          # 代码根目录（git clone 的位置）

## 后端
BACKEND_RUN=$PROJECT_ROOT/run/backend         # 运行目录
BACKEND_JAR=$BACKEND_RUN/app.jar              # 固定运行 jar 名
BACKEND_CFG=$BACKEND_RUN/config              # 外置配置目录（application-prod.yml 在这里）
BACKEND_LOG=$BACKEND_RUN/logs/app.log         # 日志文件
BACKEND_PID=$BACKEND_RUN/deepay.pid           # PID 文件
BACKEND_PORT=48080                            # Spring Boot 监听端口
BACKEND_PROFILE=prod                          # --spring.profiles.active
JAVA_OPTS="-server -Xms512m -Xmx512m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${BACKEND_RUN}/heapDump"

## 前端
FRONTEND_SRC=$PROJECT_ROOT/deepay-pwa          # PWA 前端源码目录（含 /admin 路由）
FRONTEND_DIST=$FRONTEND_SRC/dist             # Vite 构建输出
FRONTEND_DEPLOY=/www/wwwroot/deepay.srl      # Nginx root：同时服务 Web 入口 + PWA + /admin

## 备份
BACKUP_DIR=$BACKEND_RUN/backup
# ============================================================

DATE=$(date '+%Y%m%d_%H%M%S')

# ── 彩色输出 ─────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; N='\033[0m'
info()  { echo -e "${G}[INFO ]${N}  $*"; }
warn()  { echo -e "${Y}[WARN ]${N}  $*"; }
error() { echo -e "${R}[ERROR]${N}  $*"; exit 1; }
step()  { echo -e "\n${C}══════════ $* ══════════${N}"; }
ok()    { echo -e "${G}  ✓  $*${N}"; }

need() { command -v "$1" &>/dev/null || error "缺少命令: $1  →  请先在宝塔软件商店安装"; }

# ============================================================
# 步骤 0：初始化目录
# ============================================================
mkdir -p "$BACKEND_RUN/logs" "$BACKEND_CFG" "$BACKUP_DIR"

# ============================================================
# 步骤 A：后端部署
# ============================================================
deploy_backend() {

  step "A-1  环境检查"
  need java; need mvn; need curl
  info "Java  : $(java  -version 2>&1 | head -1)"
  info "Maven : $(mvn   -version 2>&1 | head -1)"

  # ── 把仓库内的 prod 配置同步到运行目录 ──
  step "A-2  同步外置配置"
  SRC_CFG=$PROJECT_ROOT/run/backend/config/application-prod.yml
  DST_CFG=$BACKEND_CFG/application-prod.yml
  if [ ! -f "$SRC_CFG" ]; then
    warn "未找到 $SRC_CFG，将使用已有配置（若无配置则启动会失败）"
  elif [ "$SRC_CFG" -ef "$DST_CFG" ]; then
    ok "配置已在运行目录，无需同步: $DST_CFG"
  else
    cp -f "$SRC_CFG" "$DST_CFG"
    ok "已同步 application-prod.yml → $BACKEND_CFG/"
  fi

  # ── Maven 打包（两段式，解决 yudao-dependencies SNAPSHOT 无法远程解析问题）──
  step "A-3  Maven 打包（跳过测试）"
  cd "$PROJECT_ROOT"
  BUILD_LOG=$BACKUP_DIR/build-${DATE}.log

  # 第一段：清除失败缓存，单独安装 yudao-dependencies BOM 到本地 .m2
  # 原因：yudao-dependencies 从未发布到远程仓库，-pl yudao-server -am 不包含它，
  #       Maven 在 model-building 阶段就会报 "Non-resolvable import POM"。
  rm -rf ~/.m2/repository/cn/iocoder/boot/yudao-dependencies/ 2>/dev/null || true
  mvn install -f "$PROJECT_ROOT/yudao-dependencies/pom.xml" \
    -DskipTests --no-transfer-progress -q \
    2>&1 | tee -a "$BUILD_LOG" || error "yudao-dependencies 预安装失败，查看: $BUILD_LOG"
  ok "yudao-dependencies BOM 已就绪"

  # 第二段：正常构建主项目（BOM 已在本地 .m2，不再远程查找）
  mvn clean package -DskipTests --batch-mode -pl yudao-server -am \
    2>&1 | tee "$BUILD_LOG" | grep -E "BUILD|ERROR|WARNING|INFO.*yudao-server" || true
  JAR_FILE=$(find "$PROJECT_ROOT/yudao-server/target" -maxdepth 1 -name "*.jar" \
             ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1)
  [ -f "$JAR_FILE" ] || error "打包失败，未找到 jar。完整日志: $BUILD_LOG"
  ok "打包成功: $JAR_FILE"

  # ── 备份旧 jar ──────────────────────────────────────────
  step "A-4  备份旧 jar"
  if [ -f "$BACKEND_JAR" ]; then
    cp -f "$BACKEND_JAR" "$BACKUP_DIR/app-${DATE}.jar"
    ok "旧 jar 已备份 → $BACKUP_DIR/app-${DATE}.jar"
  fi
  cp -f "$JAR_FILE" "$BACKEND_JAR"
  ok "新 jar → $BACKEND_JAR"

  # ── 停止旧进程 ──────────────────────────────────────────
  step "A-5  停止旧后端进程"
  _stop_backend

  # ── 启动新进程 ──────────────────────────────────────────
  step "A-6  启动 Spring Boot"
  cd "$BACKEND_RUN"
  # shellcheck disable=SC2086
  nohup java $JAVA_OPTS \
    -jar "$BACKEND_JAR" \
    --spring.profiles.active=$BACKEND_PROFILE \
    --spring.config.additional-location=file:./config/ \
    >> "$BACKEND_LOG" 2>&1 &
  echo $! > "$BACKEND_PID"
  ok "已启动  PID=$(cat "$BACKEND_PID")  日志→ $BACKEND_LOG"

  # ── 健康检查 ────────────────────────────────────────────
  step "A-7  健康检查（最长等 120s）"
  HEALTH=http://127.0.0.1:${BACKEND_PORT}/actuator/health
  STATUS=000
  for i in $(seq 1 120); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH" 2>/dev/null || echo "000")
    [ "$STATUS" = "200" ] && break
    printf "."
    sleep 1
  done
  echo ""
  if [ "$STATUS" = "200" ]; then
    ok "健康检查通过 ✓  $HEALTH"
  else
    warn "120s 内未就绪（最后状态 $STATUS）。查看日志："
    tail -30 "$BACKEND_LOG" || true
    warn "后端可能仍在启动中，稍后再检查：curl $HEALTH"
  fi
}

# ── 停止后端（供内部调用）───────────────────────────────────
_stop_backend() {
  if [ -f "$BACKEND_PID" ]; then
    OLD=$(cat "$BACKEND_PID")
    if kill -0 "$OLD" 2>/dev/null; then
      info "正在停止 PID=$OLD ..."
      kill -15 "$OLD" 2>/dev/null || true
      for i in $(seq 1 30); do
        kill -0 "$OLD" 2>/dev/null || { ok "进程已停止"; return; }
        sleep 1; printf "."
      done
      echo ""
      kill -9 "$OLD" 2>/dev/null || true
      warn "已强制 kill -9 $OLD"
    else
      warn "PID=$OLD 进程不存在，跳过"
    fi
    rm -f "$BACKEND_PID"
  else
    # 兜底：按端口找进程
    OLD=$(ss -lntp 2>/dev/null | grep ":${BACKEND_PORT}" | grep -oP 'pid=\K[0-9]+' | head -1 || true)
    if [ -n "$OLD" ]; then
      warn "发现端口 $BACKEND_PORT 被 PID=$OLD 占用，强制停止"
      kill -9 "$OLD" 2>/dev/null || true
    else
      info "未发现运行中的后端进程"
    fi
  fi
}

# ============================================================
# 步骤 B：前端部署
# ============================================================
deploy_frontend() {

  step "B-1  环境检查"
  need node; need npm
  info "Node : $(node -v)"
  info "npm  : $(npm  -v)"

  step "B-2  安装依赖"
  cd "$FRONTEND_SRC"
  npm install --prefer-offline 2>&1 | tail -3 \
    || npm install --legacy-peer-deps 2>&1 | tail -3 \
    || error "npm install 失败"
  ok "依赖安装完毕"

  step "B-3  Vite 构建（NODE_ENV=production）"
  npm run build 2>&1 | tail -8
  [ -d "$FRONTEND_DIST" ] || error "构建失败，未找到 $FRONTEND_DIST"
  ok "构建完成 → $FRONTEND_DIST"

  step "B-4  部署到 $FRONTEND_DEPLOY"
  # 备份旧前端
  if [ -d "$FRONTEND_DEPLOY" ] && [ "$(ls -A "$FRONTEND_DEPLOY" 2>/dev/null)" ]; then
    FRONT_BAK=$BACKUP_DIR/frontend-dist-${DATE}.tar.gz
    tar -czf "$FRONT_BAK" -C "$FRONTEND_DEPLOY" . 2>/dev/null \
      && ok "旧前端已备份 → $FRONT_BAK" || true
  fi
  mkdir -p "$FRONTEND_DEPLOY"
  rm -rf "${FRONTEND_DEPLOY:?}"/*
  cp -r "$FRONTEND_DIST"/. "$FRONTEND_DEPLOY/"
  ok "前端已部署 → $FRONTEND_DEPLOY"

  step "B-5  重载 Nginx"
  if nginx -t 2>/dev/null; then
    systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null || \
      warn "Nginx 重载失败，请手动执行: nginx -s reload"
    ok "Nginx 已重载"
  else
    warn "Nginx 配置校验失败，请检查配置后手动 reload"
  fi
}

# ============================================================
# 主流程
# ============================================================
echo -e "${C}"
echo "  ██████╗ ███████╗███████╗██████╗  █████╗ ██╗   ██╗"
echo "  ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗╚██╗ ██╔╝"
echo "  ██║  ██║█████╗  █████╗  ██████╔╝███████║ ╚████╔╝ "
echo "  ██║  ██║██╔══╝  ██╔══╝  ██╔═══╝ ██╔══██║  ╚██╔╝  "
echo "  ██████╔╝███████╗███████╗██║     ██║  ██║   ██║   "
echo "  ╚═════╝ ╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝   ╚═╝   "
echo -e "${N}"
info "模式: $MODE   时间: $DATE"
info "项目根: $PROJECT_ROOT"

case "$MODE" in
  backend)   deploy_backend   ;;
  frontend)  deploy_frontend  ;;
  all)       deploy_backend && deploy_frontend ;;
  *)         error "未知模式: $MODE  →  用法: $0 [all|backend|frontend]" ;;
esac

# ============================================================
# 完成汇总
# ============================================================
step "🚀  部署完成"
echo ""
echo "  ┌──────────────────────────────────────────────────────────────┐"
echo "  │  后端端口  http://127.0.0.1:${BACKEND_PORT}                     │"
echo "  │  后端日志  $BACKEND_LOG"
echo "  │  PID 文件  $BACKEND_PID"
echo "  │  外置配置  $BACKEND_CFG/application-prod.yml"
echo "  │  前端目录  $FRONTEND_DEPLOY"
echo "  │  Swagger   http://127.0.0.1:${BACKEND_PORT}/swagger-ui"
echo "  ├──────────────────────────────────────────────────────────────┤"
echo "  │  域名访问（HTTPS 须先在宝塔申请 SSL 证书）                   │"
echo "  │                                                              │"
echo "  │  🌐 Web 入口（用户）                                        │"
echo "  │     https://deepay.srl          主站 PWA（中文）             │"
echo "  │     https://modaui.com          主站 PWA（国际）             │"
echo "  │                                                              │"
echo "  │  📱 手机 App 入口                                           │"
echo "  │     浏览器访问上方任一域名 → 浏览器菜单「添加到主屏幕」     │"
echo "  │     iOS Safari: 分享 → 添加到主屏幕                         │"
echo "  │     Android Chrome: 菜单 → 安装应用                         │"
echo "  │                                                              │"
echo "  │  ⚙️  管理后台                                               │"
echo "  │     https://admin.deepay.srl    独立管理后台入口             │"
echo "  │     https://deepay.srl/admin    前台侧栏「管理后台」按钮     │"
echo "  │                                                              │"
echo "  │  🔌 API 接口                                                │"
echo "  │     https://api.deepay.srl      Spring Boot REST API         │"
echo "  │     https://deepay.srl/api/     同上（Nginx 反代）           │"
echo "  │                                                              │"
echo "  │  🤖 AI 开店                                                 │"
echo "  │     https://ai.deepay.srl       AI 开店页独立入口            │"
echo "  │     https://deepay.srl/ai-sales 同上（前台直达）             │"
echo "  └──────────────────────────────────────────────────────────────┘"
echo ""
echo "  Nginx 多域名配置参见: docs/deepay/deploy-guide.md"
