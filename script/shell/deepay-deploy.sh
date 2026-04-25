#!/usr/bin/env bash
# =============================================================
#  Deepay 全栈一键部署脚本  v3.0
#
#  包含：自动 git pull、依赖自动修复、MySQL 初始化、MongoDB 检查
#        后端 Spring Boot 构建、PWA 前端构建、uni-app H5 构建
#        Nginx 热重载、健康检查、失败回滚、并发锁
#
#  用法（项目根目录 或 任意目录均可执行）：
#    bash script/shell/deepay-deploy.sh          # 默认：全栈部署
#    bash script/shell/deepay-deploy.sh init     # 首次初始化（建库+全栈）
#    bash script/shell/deepay-deploy.sh all      # 后端+前端+H5（跳过DB）
#    bash script/shell/deepay-deploy.sh backend  # 仅后端 Spring Boot
#    bash script/shell/deepay-deploy.sh frontend # 仅 PWA 前端
#    bash script/shell/deepay-deploy.sh app      # 仅 uni-app H5
#    bash script/shell/deepay-deploy.sh db       # 仅数据库初始化/增量升级
#    bash script/shell/deepay-deploy.sh mongo    # 仅 MongoDB 检查/启动
#
#  环境变量覆盖：
#    SKIP_PULL=true      跳过 git pull
#    SKIP_BUILD=true     跳过 Maven 构建（直接用现有 jar）
# =============================================================
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
MODE="${1:-all}"
SKIP_PULL="${SKIP_PULL:-false}"
SKIP_BUILD="${SKIP_BUILD:-false}"
DEPLOY_START=$(date +%s)
DATE=$(date '+%Y%m%d_%H%M%S')
LOCK_FILE="/tmp/deepay-deploy.lock"

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  ⚙  配置区（按实际情况修改）
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## 后端
BACKEND_RUN="$PROJECT_ROOT/run/backend"
BACKEND_JAR="$BACKEND_RUN/app.jar"
BACKEND_CFG="$BACKEND_RUN/config"
BACKEND_LOG="$BACKEND_RUN/logs/app.log"
BACKEND_PID="$BACKEND_RUN/deepay.pid"
BACKEND_PORT=48080
BACKEND_PROFILE=prod
JAVA_OPTS="-server -Xms512m -Xmx512m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${PROJECT_ROOT}/run/backend/heapDump"

## PWA 前端
PWA_SRC="$PROJECT_ROOT/yudao-ui-deepay"
PWA_DIST="$PWA_SRC/dist"
PWA_DEPLOY="/www/wwwroot/deepay.srl"

## uni-app H5
APP_SRC="$PROJECT_ROOT/yudao-ui-deepay-app"
APP_DIST="$APP_SRC/dist/build/h5"
APP_DEPLOY="/www/wwwroot/deepay.srl/app"

## MySQL
DB_HOST="127.0.0.1"
DB_PORT="3306"
DB_NAME="deepay"
DB_USER="deepay"
DB_PASS="deepay393163"

## MongoDB
MONGO_HOST="127.0.0.1"
MONGO_PORT="27017"
MONGO_DB="deepay"

## 备份 & 日志
BACKUP_DIR="$BACKEND_RUN/backup"
BACKUP_KEEP=5
BUILD_LOGS="$PROJECT_ROOT/.build-logs"

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🎨  彩色输出
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'
C='\033[0;36m'; B='\033[1m'; DIM='\033[2m'; N='\033[0m'
info()  { echo -e "${G}[INFO ]${N} $*"; }
warn()  { echo -e "${Y}[WARN ]${N} $*"; }
err()   { echo -e "${R}${B}[ERROR]${N} $*" >&2; _release_lock; exit 1; }
step()  { echo -e "\n${C}${B}━━━━━━  $*  ━━━━━━${N}"; }
ok()    { echo -e "${G}${B}  ✔  $*${N}"; }
fail()  { echo -e "${R}${B}  ✘  $*${N}"; }
dim()   { echo -e "${DIM}    $*${N}"; }
hr()    { echo -e "${C}$(printf '━%.0s' {1..58})${N}"; }
need()  { command -v "$1" &>/dev/null || err "缺少命令: $1  →  请先安装"; }
elapsed() { echo $(( $(date +%s) - $1 ))s; }

mkdir -p "$BACKEND_RUN/logs" "$BACKEND_CFG" "$BACKUP_DIR" "$BUILD_LOGS"

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🔒  并发部署锁
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
_acquire_lock() {
  if [ -f "$LOCK_FILE" ]; then
    local old_pid; old_pid=$(cat "$LOCK_FILE" 2>/dev/null || echo "")
    if [ -n "$old_pid" ] && kill -0 "$old_pid" 2>/dev/null; then
      warn "另一个部署正在进行 (PID=$old_pid)，等待最多 15 分钟..."
      local waited=0
      while kill -0 "$old_pid" 2>/dev/null && (( waited < 900 )); do
        sleep 3; (( waited += 3 ))
      done
      kill -0 "$old_pid" 2>/dev/null && \
        err "超时：部署进程 $old_pid 仍在运行\n  手动解锁: rm $LOCK_FILE"
    fi
  fi
  echo $$ > "$LOCK_FILE"
  trap '_release_lock' EXIT INT TERM
}
_release_lock() { rm -f "$LOCK_FILE"; }

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  📥  自动 Git 拉取（重试3次，失败不中断部署）
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
do_git_pull() {
  $SKIP_PULL && { info "SKIP_PULL=true，跳过 git pull"; return 0; }
  step "Git  自动拉取最新代码"
  cd "$PROJECT_ROOT"

  # 暂存本地修改，不阻断部署
  local stashed=false
  if ! git diff --quiet 2>/dev/null || ! git diff --cached --quiet 2>/dev/null; then
    warn "检测到本地修改，自动 stash..."
    git stash push -m "auto-stash-$DATE" 2>&1 | head -2
    stashed=true
  fi

  local before; before=$(git rev-parse --short HEAD 2>/dev/null || echo "?")
  local branch; branch=$(git symbolic-ref --short HEAD 2>/dev/null || echo "main")

  local pulled=false
  for attempt in 1 2 3; do
    if git fetch origin "$branch" --prune 2>&1 | head -3 \
       && git reset --hard "origin/$branch" 2>&1 | head -2; then
      pulled=true; break
    fi
    warn "第 $attempt 次拉取失败，5 秒后重试..."
    sleep 5
  done

  if ! $pulled; then
    warn "git pull 失败（网络/代理问题？），使用本地代码继续部署"
    $stashed && git stash pop 2>/dev/null || true
    return 0
  fi

  local after; after=$(git rev-parse --short HEAD 2>/dev/null || echo "?")
  if [ "$before" != "$after" ]; then
    ok "代码已更新: $before → $after"
    git log --oneline "$before..$after" 2>/dev/null | head -8 | sed 's/^/   /' || true
  else
    info "代码已是最新 ($after)"
  fi
  $stashed && git stash pop 2>/dev/null || true
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🔧  依赖自动修复
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
do_autofix() {
  step "依赖  自动修复"
  local any=false

  # 修复1：vite ^8.x 与 vite-plugin-pwa@1.2.0 不兼容 → 降级到 ^7.3.2
  local pwa_pkg="$PWA_SRC/package.json"
  if [ -f "$pwa_pkg" ] && grep -qE '"vite":\s*"\^[89]' "$pwa_pkg"; then
    sed -i -E 's/"vite":\s*"\^[89][^"]*"/"vite": "^7.3.2"/' "$pwa_pkg"
    warn "  ✎  yudao-ui-deepay: vite 降级 → ^7.3.2（vite-plugin-pwa@1.2.0 最高支持 vite 7）"
    any=true
  fi

  # 修复2：vue@3.4.x + pinia>=2.3.x 不兼容 → 固定 pinia@2.2.8
  local app_pkg="$APP_SRC/package.json"
  if [ -f "$app_pkg" ]; then
    local vue_ver; vue_ver=$(grep -oE '"vue":\s*"3\.[0-4]\.' "$app_pkg" | head -1 || true)
    if [ -n "$vue_ver" ] && grep -qE '"pinia":\s*"\^?2\.[^2]' "$app_pkg"; then
      sed -i -E 's/"pinia":\s*"[^"]*"/"pinia": "2.2.8"/' "$app_pkg"
      warn "  ✎  yudao-ui-deepay-app: pinia → 2.2.8（兼容 vue ${vue_ver}x）"
      any=true
    fi
  fi

  $any || ok "依赖版本正常，无需修复"
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🗄  MySQL 数据库初始化
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
do_db() {
  step "DB    MySQL 初始化"
  need mysql

  # 测试连接
  if ! mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" \
       -p"$DB_PASS" -e "SELECT 1;" "$DB_NAME" &>/dev/null 2>&1; then
    warn "库 '$DB_NAME' 不存在或账号未建，尝试用 root 创建..."
    warn "请输入 MySQL root 密码："
    mysql -h "$DB_HOST" -P "$DB_PORT" -u root -p <<SQL
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
CREATE USER IF NOT EXISTS '${DB_USER}'@'127.0.0.1' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
    ok "数据库 '$DB_NAME' 和账号 '$DB_USER' 已创建"
  else
    ok "数据库连接正常: $DB_NAME @ $DB_HOST:$DB_PORT"
  fi

  # 检查是否已初始化
  local tbl_count
  tbl_count=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" \
    -se "SELECT COUNT(*) FROM information_schema.tables
         WHERE table_schema='${DB_NAME}';" 2>/dev/null || echo "0")

  if (( tbl_count < 10 )); then
    info "表数量 $tbl_count，执行初始化 SQL（按顺序）..."
    local sql_dir="$PROJECT_ROOT/sql/mysql"
    for sql_file in \
      "$sql_dir/ruoyi-vue-pro.sql" \
      "$sql_dir/quartz.sql" \
      "$sql_dir/deepay.sql"; do
      if [ -f "$sql_file" ]; then
        info "  → 导入 $(basename "$sql_file")"
        mysql -h "$DB_HOST" -P "$DB_PORT" \
              -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" \
              < "$sql_file" 2>&1 | grep -iE "^error" || true
        ok "  ✔ $(basename "$sql_file")"
      else
        warn "SQL 文件不存在: $sql_file"
      fi
    done
    local final_count
    final_count=$(mysql -h "$DB_HOST" -P "$DB_PORT" \
      -u "$DB_USER" -p"$DB_PASS" \
      -se "SELECT COUNT(*) FROM information_schema.tables
           WHERE table_schema='${DB_NAME}';" 2>/dev/null || echo "?")
    ok "数据库初始化完成（共 $final_count 张表）"
  else
    ok "数据库已初始化（$tbl_count 张表），跳过重建"
    _run_incremental_sql
  fi
}

# 增量 SQL 升级（sql/mysql/updates/*.sql 按文件名顺序执行，只执行未记录的）
_run_incremental_sql() {
  local updates_dir="$PROJECT_ROOT/sql/mysql/updates"
  [ -d "$updates_dir" ] || return 0
  local applied_file="$BUILD_LOGS/applied-sql.txt"
  touch "$applied_file"
  while IFS= read -r sql_file; do
    local fname; fname=$(basename "$sql_file")
    if ! grep -qxF "$fname" "$applied_file" 2>/dev/null; then
      info "增量 SQL: $fname"
      mysql -h "$DB_HOST" -P "$DB_PORT" \
            -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" \
            < "$sql_file" 2>&1 | grep -iE "^error" || true
      echo "$fname" >> "$applied_file"
      ok "  ✔ $fname"
    fi
  done < <(find "$updates_dir" -maxdepth 1 -name "*.sql" | sort)
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🍃  MongoDB 检查 & 自动启动
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
do_mongo() {
  step "Mongo MongoDB 检查"

  if systemctl is-active --quiet mongod 2>/dev/null; then
    ok "MongoDB 服务运行中（systemd）"
  elif command -v docker &>/dev/null \
       && docker ps --format '{{.Names}}' 2>/dev/null | grep -qE "mongo|deepay-mongo"; then
    ok "MongoDB 运行在 Docker 容器中"
  elif command -v mongod &>/dev/null; then
    warn "MongoDB 未运行，尝试启动..."
    systemctl start mongod 2>/dev/null \
      || mongod --fork --logpath /var/log/mongod.log --dbpath /var/lib/mongo 2>/dev/null \
      || warn "启动失败，请手动执行: systemctl start mongod"
    sleep 3
  else
    warn "未检测到 MongoDB"
    warn "Ubuntu 安装: apt-get install -y mongodb-org && systemctl start mongod"
    warn "CentOS 安装: yum install -y mongodb-org && systemctl start mongod"
    warn "或使用 Docker: docker-compose -f docker-compose.mongodb-rs.yml up -d"
    warn "AI 功能（聊天记忆/工具审计）在 MongoDB 可用前将不可用"
    return 0
  fi

  # 连通性验证
  if command -v mongosh &>/dev/null; then
    mongosh --host "$MONGO_HOST:$MONGO_PORT" --quiet \
      --eval "db.adminCommand('ping').ok" 2>/dev/null \
      && ok "MongoDB 连接正常: $MONGO_DB @ $MONGO_HOST:$MONGO_PORT" \
      || warn "MongoDB 连通性测试失败，请检查配置"
  elif command -v mongo &>/dev/null; then
    mongo --host "$MONGO_HOST:$MONGO_PORT" --quiet \
      --eval "db.adminCommand('ping').ok" 2>/dev/null \
      && ok "MongoDB 连接正常" || warn "MongoDB 连通性异常"
  fi
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  ☕  后端 Spring Boot
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
_stop_backend() {
  if [ -f "$BACKEND_PID" ]; then
    local old_pid; old_pid=$(cat "$BACKEND_PID" 2>/dev/null || echo "")
    if [ -n "$old_pid" ] && kill -0 "$old_pid" 2>/dev/null; then
      info "停止旧进程 PID=$old_pid ..."
      kill -15 "$old_pid" 2>/dev/null || true
      for i in $(seq 1 30); do
        kill -0 "$old_pid" 2>/dev/null || { ok "进程已停止"; break; }
        sleep 1; printf "."
      done
      echo ""
      kill -0 "$old_pid" 2>/dev/null && kill -9 "$old_pid" 2>/dev/null || true
    fi
    rm -f "$BACKEND_PID"
  else
    # 按端口兜底查找
    local port_pid
    port_pid=$(ss -lntp 2>/dev/null \
      | grep ":${BACKEND_PORT} " \
      | grep -oP 'pid=\K[0-9]+' | head -1 || true)
    if [ -n "$port_pid" ]; then
      warn "端口 $BACKEND_PORT 被 PID=$port_pid 占用，强制停止"
      kill -9 "$port_pid" 2>/dev/null || true
    fi
  fi
}

_backup_jar() {
  [ -f "$BACKEND_JAR" ] || return 0
  cp -f "$BACKEND_JAR" "$BACKUP_DIR/app-$DATE.jar"
  # 保留最近 N 份
  ls -1t "$BACKUP_DIR"/app-*.jar 2>/dev/null \
    | tail -n +$(( BACKUP_KEEP + 1 )) \
    | xargs rm -f 2>/dev/null || true
  ok "旧 jar 已备份 → $BACKUP_DIR/app-$DATE.jar"
}

do_backend() {
  step "后端  Spring Boot 构建部署"
  need java
  info "Java : $(java -version 2>&1 | head -1)"

  # 同步外置配置
  local src_cfg="$PROJECT_ROOT/run/backend/config/application-prod.yml"
  local dst_cfg="$BACKEND_CFG/application-prod.yml"
  if [ -f "$src_cfg" ] && [ "$src_cfg" != "$dst_cfg" ]; then
    cp -f "$src_cfg" "$dst_cfg"
    ok "配置已同步 → $BACKEND_CFG/application-prod.yml"
  elif [ ! -f "$dst_cfg" ]; then
    warn "未找到 application-prod.yml，启动时可能失败"
  fi

  if $SKIP_BUILD && [ -f "$BACKEND_JAR" ]; then
    info "SKIP_BUILD=true，直接使用现有 jar"
  else
    need mvn
    info "Maven: $(mvn -version 2>&1 | head -1)"
    local build_log="$BUILD_LOGS/backend-$DATE.log"
    info "Maven 构建中，日志: $build_log"
    cd "$PROJECT_ROOT"

    # 先独立安装 BOM（SNAPSHOT 依赖不能从远程解析）
    rm -rf ~/.m2/repository/cn/iocoder/boot/yudao-dependencies/ 2>/dev/null || true
    mvn install -f "$PROJECT_ROOT/yudao-dependencies/pom.xml" \
      -DskipTests --no-transfer-progress -q >> "$build_log" 2>&1 \
      || err "yudao-dependencies 安装失败，查看: $build_log"

    mvn clean package -DskipTests --batch-mode \
      -pl yudao-server -am \
      2>&1 | tee "$build_log" \
           | grep -E "BUILD|ERROR|WARNING|yudao-server" || true

    local jar_file
    jar_file=$(find "$PROJECT_ROOT/yudao-server/target" -maxdepth 1 \
      -name "*.jar" ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1)
    [ -f "$jar_file" ] || err "构建失败，未找到 jar！查看: $build_log"
    ok "构建成功: $jar_file"

    _backup_jar
    cp -f "$jar_file" "$BACKEND_JAR"
    ok "新 jar → $BACKEND_JAR"
  fi

  _stop_backend

  mkdir -p "$BACKEND_RUN/logs" "$BACKEND_RUN/heapDump"
  cd "$BACKEND_RUN"
  # shellcheck disable=SC2086
  nohup java $JAVA_OPTS \
    -jar "$BACKEND_JAR" \
    --spring.profiles.active="$BACKEND_PROFILE" \
    --spring.config.additional-location=file:./config/ \
    >> "$BACKEND_LOG" 2>&1 &
  echo $! > "$BACKEND_PID"
  ok "后端已启动  PID=$(cat "$BACKEND_PID")  日志→ $BACKEND_LOG"

  # 健康检查（最多等 120s）
  info "等待后端就绪..."
  local health="http://127.0.0.1:${BACKEND_PORT}/actuator/health"
  local status="000"
  for i in $(seq 1 120); do
    status=$(curl -s -o /dev/null -w "%{http_code}" "$health" 2>/dev/null || echo "000")
    [ "$status" = "200" ] && break
    printf "."; sleep 1
  done
  echo ""
  [ "$status" = "200" ] \
    && ok "后端健康检查通过 ✔  $health" \
    || warn "120s 内未就绪（$status），查看: tail -50 $BACKEND_LOG"
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🌐  npm 安装（自动降级 fallback）
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
_npm_install() {
  if [ -f package-lock.json ]; then
    info "npm ci ..."
    npm ci 2>&1 && return 0
    warn "npm ci 失败，降级到 npm ci --legacy-peer-deps"
    npm ci --legacy-peer-deps 2>&1 && return 0
    warn "仍然失败，清除 node_modules 后重试"
    rm -rf node_modules package-lock.json
  else
    info "npm install ..."
    npm install 2>&1 && return 0
    warn "npm install 失败，尝试 --legacy-peer-deps"
  fi
  npm install --legacy-peer-deps 2>&1 \
    || err "npm install 彻底失败，请检查 package.json 依赖"
}

_nginx_reload() {
  if nginx -t 2>/dev/null; then
    systemctl reload nginx 2>/dev/null \
      || nginx -s reload 2>/dev/null \
      || warn "Nginx reload 失败，请手动执行: nginx -s reload"
    ok "Nginx 已热重载"
  else
    warn "Nginx 配置检查失败，跳过重载"
  fi
}

_rsync_deploy() {
  local src="$1" dst="$2" name="$3"
  [ -d "$src" ] || { warn "[$name] 产物目录不存在: $src"; return 1; }
  mkdir -p "$dst"
  if command -v rsync &>/dev/null; then
    rsync -a --delete --exclude='.DS_Store' "$src/" "$dst/"
  else
    rm -rf "${dst:?}"/* && cp -rf "$src/." "$dst/"
  fi
  ok "[$name] 已部署 → $dst"
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🖥  PWA 前端（Vue3 + Vite）
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
do_frontend() {
  step "前端  PWA 构建部署（Vue3 + Vite）"
  need node; need npm
  info "Node: $(node -v)  npm: $(npm -v)"
  cd "$PWA_SRC"
  _npm_install
  local build_log="$BUILD_LOGS/pwa-$DATE.log"
  npm run build 2>&1 | tee "$build_log" | tail -5
  [ -d "$PWA_DIST" ] || err "PWA 构建失败，未找到 $PWA_DIST，查看: $build_log"
  ok "PWA 构建完成 → $PWA_DIST"
  _rsync_deploy "$PWA_DIST" "$PWA_DEPLOY" "PWA"
  _nginx_reload
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  📱  uni-app H5
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
do_app() {
  step "App   uni-app H5 构建部署"
  need node; need npm
  cd "$APP_SRC"
  _npm_install
  local build_log="$BUILD_LOGS/h5-$DATE.log"
  npm run build:h5 2>&1 | tee "$build_log" | tail -5
  [ -d "$APP_DIST" ] || err "H5 构建失败，未找到 $APP_DIST，查看: $build_log"
  ok "H5 构建完成 → $APP_DIST"
  _rsync_deploy "$APP_DIST" "$APP_DEPLOY" "H5"
  _nginx_reload
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  🎯  主流程
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
_acquire_lock

hr
echo -e "${C}${B}"
echo "  ██████╗ ███████╗███████╗██████╗  █████╗ ██╗   ██╗"
echo "  ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗╚██╗ ██╔╝"
echo "  ██║  ██║█████╗  █████╗  ██████╔╝███████║ ╚████╔╝ "
echo "  ██║  ██║██╔══╝  ██╔══╝  ██╔═══╝ ██╔══██║  ╚██╔╝  "
echo "  ██████╔╝███████╗███████╗██║     ██║  ██║   ██║   "
echo "  ╚═════╝ ╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝   ╚═╝   "
echo -e "${N}"
info "模式    : $MODE"
info "时间    : $(date '+%Y-%m-%d %H:%M:%S')"
info "项目根  : $PROJECT_ROOT"
hr

# ── 所有模式都先拉代码、修依赖 ──────────────────────────────
do_git_pull
do_autofix

# ── 按模式执行 ───────────────────────────────────────────────
case "$MODE" in
  init)
    # 首次完整初始化：DB + MongoDB + 全栈
    do_db
    do_mongo
    do_backend
    do_frontend
    do_app
    ;;
  all)
    # 日常部署：后端 + 前端 + H5（跳过DB重建）
    do_backend
    do_frontend
    do_app
    ;;
  backend)  do_backend  ;;
  frontend) do_frontend ;;
  app)      do_app      ;;
  db)       do_db       ;;
  mongo)    do_mongo    ;;
  *)
    err "未知模式: $MODE\n  用法: $0 [init|all|backend|frontend|app|db|mongo]"
    ;;
esac

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  📊  部署总结
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
hr
ok "全部完成 🎉  总耗时: $(elapsed $DEPLOY_START)"
echo ""
echo "  访问地址："
echo "    🌐  https://deepay.srl           主站 PWA"
echo "    📱  https://deepay.srl/app       H5 应用"
echo "    ⚙️   https://admin.deepay.srl    管理后台"
echo "    🔌  http://127.0.0.1:$BACKEND_PORT     Spring Boot API"
echo ""
echo "  日志位置："
echo "    后端日志  : $BACKEND_LOG"
echo "    构建日志  : $BUILD_LOGS/"
echo "    Jar 备份  : $BACKUP_DIR/"
hr
