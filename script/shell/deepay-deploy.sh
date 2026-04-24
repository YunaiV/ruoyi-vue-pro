#!/bin/bash
# =============================================================
#  Deepay 日常更新部署脚本
#
#  用法（git pull 后执行）：
#    bash script/shell/deepay-deploy.sh            # 后端 + 前端
#    bash script/shell/deepay-deploy.sh backend    # 仅后端
#    bash script/shell/deepay-deploy.sh frontend   # 仅前端
#
#  首次部署请使用：
#    bash script/shell/quickstart.sh
# =============================================================
set -euo pipefail

MODE="${1:-all}"

# ── 路径与参数（按需修改） ────────────────────────────────────
PROJECT_ROOT="/www/wwwroot/deepay.srl"
BACKEND_RUN="$PROJECT_ROOT/run/backend"
BACKEND_JAR="$BACKEND_RUN/app.jar"
BACKEND_CFG="$BACKEND_RUN/config"
BACKEND_LOG="$BACKEND_RUN/logs/app.log"
BACKEND_PID="$BACKEND_RUN/deepay.pid"
BACKEND_PORT=48080
BACKEND_PROFILE=prod
JAVA_OPTS="-server -Xms512m -Xmx512m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${BACKEND_RUN}/heapDump"

FRONTEND_SRC="$PROJECT_ROOT/yudao-ui-deepay"
FRONTEND_OUT="$PROJECT_ROOT"

BACKUP_DIR="$BACKEND_RUN/backup"
DATE=$(date '+%Y%m%d_%H%M%S')
# ─────────────────────────────────────────────────────────────

mkdir -p "$BACKEND_RUN/logs" "$BACKEND_CFG" "$BACKUP_DIR"

# ── 颜色 ──────────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; N='\033[0m'
ok()    { echo -e "${G}  ✓  ${*}${N}"; }
info()  { echo -e "[INFO ]  ${*}"; }
warn()  { echo -e "${Y}[WARN ]  ${*}${N}"; }
err()   { echo -e "${R}[ERROR]  ${*}${N}"; exit 1; }
step()  { echo -e "\n${C}══════════ ${*} ══════════${N}"; }

echo -e "${C}"
cat << 'EOF'
  ██████╗ ███████╗███████╗██████╗  █████╗ ██╗   ██╗
  ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗╚██╗ ██╔╝
  ██║  ██║█████╗  █████╗  ██████╔╝███████║ ╚████╔╝
  ██║  ██║██╔══╝  ██╔══╝  ██╔═══╝ ██╔══██║  ╚██╔╝
  ██████╔╝███████╗███████╗██║     ██║  ██║   ██║
  ╚═════╝ ╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝   ╚═╝
EOF
echo -e "${N}"
info "模式: $MODE   时间: $DATE"
info "项目根: $PROJECT_ROOT"

# ══════════════════════════════════════════════════════════════
# 后端部署
# ══════════════════════════════════════════════════════════════
deploy_backend() {

  step "A-1  环境检查"
  command -v java &>/dev/null || err "未找到 java"
  command -v mvn  &>/dev/null || err "未找到 mvn"
  info "Java  : $(java -version 2>&1 | head -1)"
  info "Maven : $(mvn  -version 2>&1 | head -1)"

  step "A-2  同步外置配置"
  SRC_CFG="$PROJECT_ROOT/run/backend/config/application-prod.yml"
  DST_CFG="$BACKEND_CFG/application-prod.yml"
  if [[ ! -f "$SRC_CFG" ]]; then
    warn "未找到 $SRC_CFG，使用已有配置"
  elif [[ "$SRC_CFG" -ef "$DST_CFG" ]]; then
    ok "配置已在运行目录: $DST_CFG"
  else
    cp -f "$SRC_CFG" "$DST_CFG"
    ok "已同步 application-prod.yml"
  fi

  # ── Maven 打包 ───────────────────────────────────────────────
  # 关键：yudao-dependencies 是本地子模块，未发布到任何远程仓库。
  # 必须先单独安装它到本地 .m2，再构建主项目，否则 Maven 在
  # model-building 阶段就会因找不到该 SNAPSHOT BOM 而报错。
  step "A-3  Maven 打包（跳过测试）"
  cd "$PROJECT_ROOT"
  BUILD_LOG="$BACKUP_DIR/build-${DATE}.log"

  info "清除 Maven 失败缓存..."
  rm -rf ~/.m2/repository/cn/iocoder/boot/yudao-dependencies/ 2>/dev/null || true

  info "预安装 yudao-dependencies BOM..."
  mvn install \
    -f "$PROJECT_ROOT/yudao-dependencies/pom.xml" \
    -DskipTests \
    --no-transfer-progress \
    -q \
    2>&1 | tee -a "$BUILD_LOG" \
    || err "yudao-dependencies 安装失败，查看日志: $BUILD_LOG"
  ok "yudao-dependencies BOM 已就绪"

  info "打包 yudao-server..."
  mvn clean package \
    -pl yudao-server -am \
    -DskipTests \
    --no-transfer-progress \
    -q \
    2>&1 | tee -a "$BUILD_LOG" \
    || err "打包失败，查看日志: $BUILD_LOG"

  JAR_FILE=$(find "$PROJECT_ROOT/yudao-server/target" -maxdepth 1 -name "*.jar" \
             ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1)
  [[ -f "$JAR_FILE" ]] || err "未找到 jar，查看日志: $BUILD_LOG"
  ok "打包成功: $JAR_FILE"

  step "A-4  备份 & 替换 jar"
  [[ -f "$BACKEND_JAR" ]] && cp -f "$BACKEND_JAR" "$BACKUP_DIR/app-${DATE}.jar" && ok "旧 jar 已备份"
  cp -f "$JAR_FILE" "$BACKEND_JAR"
  ok "新 jar → $BACKEND_JAR"

  step "A-5  停止旧进程"
  _stop_backend

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

  step "A-7  健康检查（最长 120s）"
  HEALTH="http://127.0.0.1:${BACKEND_PORT}/actuator/health"
  STATUS=000
  for _i in $(seq 1 120); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH" 2>/dev/null || echo "000")
    [[ "$STATUS" == "200" ]] && break
    printf "."; sleep 1
  done
  echo ""
  [[ "$STATUS" == "200" ]] \
    && ok "健康检查通过 ✓  $HEALTH" \
    || warn "120s 未就绪（状态 $STATUS），查看: tail -f $BACKEND_LOG"
}

# ── 停止后端（内部调用）──────────────────────────────────────
_stop_backend() {
  if [[ -f "$BACKEND_PID" ]]; then
    _old=$(cat "$BACKEND_PID")
    if kill -0 "$_old" 2>/dev/null; then
      info "停止 PID=$_old ..."
      kill -15 "$_old" 2>/dev/null || true
      for _i in $(seq 1 30); do
        kill -0 "$_old" 2>/dev/null || { ok "进程已停止"; break; }
        sleep 1; printf "."
      done
      echo ""
      kill -0 "$_old" 2>/dev/null && { kill -9 "$_old" 2>/dev/null || true; warn "已强制 kill -9"; }
    fi
    rm -f "$BACKEND_PID"
  fi
  # 端口兜底清理
  _lp=$(ss -lntp 2>/dev/null | grep ":${BACKEND_PORT}" | grep -oP 'pid=\K[0-9]+' | head -1 || true)
  [[ -n "$_lp" ]] && { kill -9 "$_lp" 2>/dev/null || true; warn "端口 ${BACKEND_PORT} 残留进程已清理"; }
}

# ══════════════════════════════════════════════════════════════
# 前端部署
# ══════════════════════════════════════════════════════════════
deploy_frontend() {

  step "B-1  环境检查"
  command -v node &>/dev/null || err "未找到 node"
  command -v npm  &>/dev/null || err "未找到 npm"
  info "Node : $(node -v)"
  info "npm  : $(npm  -v)"

  step "B-2  安装 npm 依赖"
  cd "$FRONTEND_SRC"
  npm install --prefer-offline 2>&1 | tail -3 \
    || npm install --legacy-peer-deps 2>&1 | tail -3 \
    || err "npm install 失败"
  ok "依赖安装完毕"

  step "B-3  Vite 构建"
  npm run build 2>&1 | tail -8
  [[ -d dist ]] || err "构建失败，未找到 dist 目录"
  ok "构建完成"

  step "B-4  部署到 $FRONTEND_OUT"
  if [[ -n "$(ls -A "$FRONTEND_OUT" 2>/dev/null)" ]]; then
    FRONT_BAK="$BACKUP_DIR/frontend-${DATE}.tar.gz"
    tar -czf "$FRONT_BAK" -C "$FRONTEND_OUT" . 2>/dev/null && ok "旧前端已备份 → $FRONT_BAK" || true
  fi
  mkdir -p "$FRONTEND_OUT"
  rm -rf "${FRONTEND_OUT:?}"/*
  cp -r dist/. "$FRONTEND_OUT/"
  ok "前端已部署 → $FRONTEND_OUT"

  step "B-5  重载 Nginx"
  nginx -t 2>/dev/null \
    && { nginx -s reload 2>/dev/null || systemctl reload nginx 2>/dev/null || true; ok "Nginx 已重载"; } \
    || warn "Nginx 配置有误，请手动检查后执行: nginx -s reload"
}

# ══════════════════════════════════════════════════════════════
# 主流程
# ══════════════════════════════════════════════════════════════
case "$MODE" in
  backend)   deploy_backend ;;
  frontend)  deploy_frontend ;;
  all)       deploy_backend && deploy_frontend ;;
  *) err "未知模式: $MODE  →  用法: $0 [all|backend|frontend]" ;;
esac

step "🚀  部署完成"
echo ""
echo "  ┌──────────────────────────────────────────────────────┐"
printf "  │  后端端口  http://127.0.0.1:%-25s│\n" "${BACKEND_PORT}"
printf "  │  后端日志  %-40s│\n" "$BACKEND_LOG"
printf "  │  前端目录  %-40s│\n" "$FRONTEND_OUT"
echo "  └──────────────────────────────────────────────────────┘"
echo ""
