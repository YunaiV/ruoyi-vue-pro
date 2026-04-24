#!/bin/bash
# =============================================================
#  Deepay 全自动一键部署脚本  v3
#
#  首次部署（建库 + 打包 + 构建 + Nginx + systemd）：
#    cd /www/wwwroot/deepay.srl
#    bash script/shell/quickstart.sh
#
#  日常更新（git pull 后）：
#    bash script/shell/deepay-deploy.sh            # 后端 + 前端
#    bash script/shell/deepay-deploy.sh backend    # 仅后端
#    bash script/shell/deepay-deploy.sh frontend   # 仅前端
# =============================================================
set -euo pipefail

# ── 可覆盖的环境变量 ──────────────────────────────────────────
PROJECT="${PROJECT:-/www/wwwroot/deepay.srl}"
PORT="${PORT:-48080}"
PROFILE="${PROFILE:-prod}"
DOMAIN="${DOMAIN:-deepay.srl}"
DB="${DB:-deepay}"
DB_USER="${DB_USER:-deepay}"
DB_PASS="${DB_PASS:-SRx3ETXjNWRKofo51jvc}"
NGINX_CONF="${NGINX_CONF:-/www/server/panel/vhost/nginx/deepay.conf}"
FRONTEND_OUT="${FRONTEND_OUT:-$PROJECT}"
NONINTERACTIVE="${NONINTERACTIVE:-0}"
# ─────────────────────────────────────────────────────────────

RUN="$PROJECT/run/backend"
CFG="$RUN/config"
JAR="$RUN/app.jar"
LOG="$RUN/logs/app.log"
PID="$RUN/deepay.pid"
DATE=$(date '+%Y%m%d_%H%M%S')
JAVA_OPTS="-server -Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${RUN}/heapDump"

SQL_FILES=("ruoyi-vue-pro.sql" "quartz.sql" "deepay.sql")

mkdir -p "$RUN/logs" "$RUN/backup" "$CFG"

# ── 颜色 ──────────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; B='\033[1m'; N='\033[0m'
ok()    { echo -e "${G}  ✓  ${*}${N}"; }
info()  { echo -e "     ${*}"; }
warn()  { echo -e "${Y}  ⚠  ${*}${N}"; }
err()   { echo -e "${R}  ✗  ${*}${N}"; exit 1; }
title() { echo -e "\n${C}${B}══════════ ${*} ══════════${N}"; }

# ══════════════════════════════════════════════════════════════
banner() {
cat << 'EOF'

  ██████╗ ███████╗███████╗██████╗  █████╗ ██╗   ██╗
  ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗╚██╗ ██╔╝
  ██║  ██║█████╗  █████╗  ██████╔╝███████║ ╚████╔╝
  ██║  ██║██╔══╝  ██╔══╝  ██╔═══╝ ██╔══██║  ╚██╔╝
  ██████╔╝███████╗███████╗██║     ██║  ██║   ██║
  ╚═════╝ ╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝   ╚═╝
         全 自 动 一 键 部 署 脚 本  v3
EOF
}
banner

# ══════════════════════════════════════════════════════════════
# 步骤 0 — 自动检测 Java（优先宝塔 JDK17）
# ══════════════════════════════════════════════════════════════
title "步骤 0  检测 Java"
_setup_java() {
  # 优先外部传入
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
    export PATH="$JAVA_HOME/bin:$PATH"
    ok "JAVA_HOME=$JAVA_HOME"
    return
  fi
  # 宝塔目录，优先 jdk-17
  if [[ -d /www/server/java ]]; then
    local _j
    _j=$(find /www/server/java -maxdepth 3 -name java -path "*/jdk-17*/bin/java" 2>/dev/null | sort -rV | head -1 || true)
    [[ -z "$_j" ]] && _j=$(find /www/server/java -maxdepth 3 -name java -path "*/bin/java" 2>/dev/null | sort -rV | head -1 || true)
    if [[ -n "$_j" && -x "$_j" ]]; then
      export JAVA_HOME; JAVA_HOME=$(dirname "$(dirname "$_j")")
      export PATH="$JAVA_HOME/bin:$PATH"
      ok "宝塔 JDK: $JAVA_HOME"
      return
    fi
  fi
  command -v java &>/dev/null && { ok "系统 java: $(command -v java)"; return; }
  err "未找到 Java！请在宝塔软件商店安装 JDK17"
}
_setup_java
info "Java  : $(java -version 2>&1 | head -1)"
command -v mvn &>/dev/null || err "未找到 Maven！请安装 Apache Maven 3.6+"
info "Maven : $(mvn -version 2>&1 | head -1)"

# ══════════════════════════════════════════════════════════════
# 步骤 1 — 环境依赖检查
# ══════════════════════════════════════════════════════════════
title "步骤 1  环境依赖检查"

_need() { command -v "$1" &>/dev/null || err "缺少命令: $1，请先安装"; }
_need nginx; _need mysql; _need node; _need npm

ok "Nginx : $(nginx -v 2>&1)"
ok "MySQL : $(mysql --version 2>&1 | head -1)"
ok "Node  : $(node -v)  npm: $(npm -v)"
command -v redis-cli &>/dev/null \
  && { redis-cli ping &>/dev/null && ok "Redis : 运行中" || warn "Redis 未响应，请检查"; } \
  || warn "redis-cli 未找到，请确认 Redis 已安装并运行"

# ══════════════════════════════════════════════════════════════
# 步骤 2 — Maven 镜像配置（仅首次写入）
# ══════════════════════════════════════════════════════════════
title "步骤 2  Maven 镜像配置"
mkdir -p ~/.m2
if grep -q "aliyunmaven" ~/.m2/settings.xml 2>/dev/null; then
  ok "Maven 镜像已配置，跳过"
else
  cat > ~/.m2/settings.xml << 'MAVEN_XML'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
            https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>central</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
    <mirror>
      <id>huaweicloud</id>
      <mirrorOf>external:*,!aliyunmaven</mirrorOf>
      <name>华为云仓库</name>
      <url>https://mirrors.huaweicloud.com/repository/maven/</url>
    </mirror>
  </mirrors>
</settings>
MAVEN_XML
  ok "Maven 镜像已写入 ~/.m2/settings.xml"
fi

# ══════════════════════════════════════════════════════════════
# 步骤 3 — 数据库创建 & SQL 导入
# ══════════════════════════════════════════════════════════════
title "步骤 3  数据库初始化"

# 获取 MySQL root 密码
_get_mysql_root() {
  local _pwd=""
  # 方式1：宝塔 config.json
  if [[ -f /www/server/panel/config/config.json ]]; then
    _pwd=$(python3 -c \
      "import json; d=json.load(open('/www/server/panel/config/config.json')); print(d.get('mysql_root',''))" \
      2>/dev/null || true)
    [[ -n "$_pwd" ]] && { echo "$_pwd"; return; }
  fi
  # 方式2：上次保存的密码
  [[ -f /root/.deepay_mysql_root ]] && { cat /root/.deepay_mysql_root; return; }
  # 方式3：交互式输入
  if [[ "${NONINTERACTIVE}" == "1" ]]; then
    err "无法自动获取 MySQL root 密码，请设置 NONINTERACTIVE=0 后重试"
  fi
  echo -n "  请输入 MySQL root 密码: " >&2
  read -rs _pwd; echo "" >&2
  echo "$_pwd"
}

ROOT_PWD=$(_get_mysql_root)
MCMD="mysql -u root -p${ROOT_PWD} --connect-timeout=5"
$MCMD -e "SELECT 1;" &>/dev/null || err "MySQL root 密码错误或 MySQL 未运行"

$MCMD <<SQL 2>/dev/null || true
CREATE DATABASE IF NOT EXISTS \`${DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON \`${DB}\`.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL
ok "数据库 ${DB}  用户 ${DB_USER} 已就绪"

for _f in "${SQL_FILES[@]}"; do
  _fp="$PROJECT/sql/mysql/$_f"
  if [[ ! -f "$_fp" ]]; then
    warn "跳过（文件不存在）: $_f"
    continue
  fi
  info "导入 $_f ..."
  if $MCMD "$DB" < "$_fp" 2>/tmp/_deepay_sql_err.txt; then
    ok "$_f 导入完成"
  else
    grep -qiE "already exists|Duplicate entry|duplicate key" /tmp/_deepay_sql_err.txt 2>/dev/null \
      && ok "$_f（部分表/数据已存在，跳过重复）" \
      || { warn "$_f 导入时有异常:"; head -3 /tmp/_deepay_sql_err.txt || true; }
  fi
done
rm -f /tmp/_deepay_sql_err.txt

# ══════════════════════════════════════════════════════════════
# 步骤 4 — 写后端生产配置
# ══════════════════════════════════════════════════════════════
title "步骤 4  写后端生产配置"
cat > "$CFG/application-prod.yml" << YAML
server:
  port: ${PORT}

spring:
  autoconfigure:
    exclude:
      - com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
      - org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration
      - org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration
      - org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration
      - com.binarywang.spring.starter.wxjava.mp.config.WxMpServiceAutoConfiguration
      - com.binarywang.spring.starter.wxjava.miniapp.config.WxMaServiceAutoConfiguration
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/${DB}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
          username: ${DB_USER}
          password: ${DB_PASS}
        slave:
          lazy: true
          url: jdbc:mysql://127.0.0.1:3306/${DB}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
          username: ${DB_USER}
          password: ${DB_PASS}
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000ms

yudao:
  web:
    admin-ui:
      url: https://${DOMAIN}
YAML
ok "已写入 $CFG/application-prod.yml"

# ══════════════════════════════════════════════════════════════
# 步骤 5 — Maven 打包后端
#
# 关键：yudao-dependencies 是本地子模块，从未发布到任何 Maven 仓库。
# 根 pom.xml 把它当外部 BOM import，Maven 在 model-building 阶段
# 就去远程查找 → 失败 → 缓存失败。
# 解决方案：先单独安装 yudao-dependencies（它无 parent，可独立构建），
# 再做正式打包。
# ══════════════════════════════════════════════════════════════
title "步骤 5  Maven 打包"
cd "$PROJECT"
BUILD_LOG="$RUN/backup/build-${DATE}.log"

info "清除 Maven 失败缓存..."
rm -rf ~/.m2/repository/cn/iocoder/boot/yudao-dependencies/ 2>/dev/null || true

info "预安装 yudao-dependencies BOM（约 10~30 秒）..."
mvn install \
  -f "$PROJECT/yudao-dependencies/pom.xml" \
  -DskipTests \
  --no-transfer-progress \
  -q \
  2>&1 | tee -a "$BUILD_LOG" \
  || err "yudao-dependencies 安装失败，查看日志: $BUILD_LOG"
ok "yudao-dependencies BOM 已安装到本地仓库"

info "打包 yudao-server（需要几分钟，请耐心等待）..."
mvn clean package \
  -pl yudao-server -am \
  -DskipTests \
  --no-transfer-progress \
  -q \
  2>&1 | tee -a "$BUILD_LOG" \
  || err "Maven 打包失败，查看日志: $BUILD_LOG"

NEW_JAR=$(find "$PROJECT/yudao-server/target" -maxdepth 1 -name "*.jar" \
          ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1)
[[ -f "$NEW_JAR" ]] || err "未找到 jar 包，查看日志: $BUILD_LOG"
ok "打包成功: $NEW_JAR"

# ══════════════════════════════════════════════════════════════
# 步骤 6 — 停止旧后端 & 启动新后端
# ══════════════════════════════════════════════════════════════
title "步骤 6  启动后端"

# 停止旧进程
if [[ -f "$PID" ]]; then
  _old=$(cat "$PID")
  if kill -0 "$_old" 2>/dev/null; then
    kill -15 "$_old" 2>/dev/null || true
    for _i in $(seq 1 30); do
      kill -0 "$_old" 2>/dev/null || break
      sleep 1; printf "."
    done
    echo ""
    kill -0 "$_old" 2>/dev/null && { kill -9 "$_old" 2>/dev/null || true; warn "已强制 kill -9"; }
    ok "旧进程已停止"
  fi
  rm -f "$PID"
fi
# 端口残留清理
_lp=$(ss -lntp 2>/dev/null | grep ":${PORT}" | grep -oP 'pid=\K[0-9]+' | head -1 || true)
[[ -n "$_lp" ]] && { kill -9 "$_lp" 2>/dev/null || true; warn "端口 ${PORT} 残留进程已清理"; }

[[ -f "$JAR" ]] && cp -f "$JAR" "$RUN/backup/app-${DATE}.jar" 2>/dev/null || true
cp -f "$NEW_JAR" "$JAR"

cd "$RUN"
# shellcheck disable=SC2086
nohup java $JAVA_OPTS \
  -jar "$JAR" \
  --spring.profiles.active=${PROFILE} \
  --spring.config.additional-location=file:./config/ \
  >> "$LOG" 2>&1 &
echo $! > "$PID"
ok "后端已启动  PID=$(cat "$PID")"
info "日志: tail -f $LOG"

info "等待后端就绪（最长 120 秒）..."
_status=000
for _i in $(seq 1 120); do
  _status=$(curl -s -o /dev/null -w "%{http_code}" \
    "http://127.0.0.1:${PORT}/actuator/health" 2>/dev/null || echo "000")
  [[ "$_status" == "200" ]] && break
  printf "."; sleep 1
done
echo ""
[[ "$_status" == "200" ]] \
  && ok "后端健康检查通过 ✓" \
  || warn "120s 未就绪（状态 $_status），仍在启动中。查看: tail -f $LOG"

# ══════════════════════════════════════════════════════════════
# 步骤 7 — 构建 & 部署前端
# ══════════════════════════════════════════════════════════════
title "步骤 7  构建前端"
cd "$PROJECT/yudao-ui-deepay"

info "安装 npm 依赖..."
npm install --prefer-offline 2>&1 | tail -3 \
  || npm install --legacy-peer-deps 2>&1 | tail -3 \
  || err "npm install 失败，请检查 Node 版本（需要 16+）"
ok "npm 依赖安装完成"

info "Vite 构建..."
npm run build 2>&1 | tail -8
[[ -d dist ]] || err "前端构建失败，未找到 dist 目录"
ok "前端构建完成"

title "步骤 8  部署前端 → $FRONTEND_OUT"
mkdir -p "$FRONTEND_OUT"
if [[ -n "$(ls -A "$FRONTEND_OUT" 2>/dev/null)" ]]; then
  tar -czf "$RUN/backup/frontend-${DATE}.tar.gz" -C "$FRONTEND_OUT" . 2>/dev/null \
    && ok "旧前端已备份" || true
fi
rm -rf "${FRONTEND_OUT:?}"/*
cp -r dist/. "$FRONTEND_OUT/"
ok "前端已部署 → $FRONTEND_OUT"

# ══════════════════════════════════════════════════════════════
# 步骤 9 — Nginx 配置
# ══════════════════════════════════════════════════════════════
title "步骤 9  配置 Nginx（${DOMAIN}）"
mkdir -p "$(dirname "$NGINX_CONF")"
[[ -f "$NGINX_CONF" ]] && cp -f "$NGINX_CONF" "${NGINX_CONF}.bak.${DATE}" && ok "旧配置已备份"

cat > "$NGINX_CONF" << NGINX_EOF
# 自动生成于 $(date) — 请勿手动编辑
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN} www.${DOMAIN};

    root ${FRONTEND_OUT};
    index index.html;
    client_max_body_size 32m;

    # Vue SPA (history 模式)
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # 后端 admin-api
    location /admin-api/ {
        proxy_pass         http://127.0.0.1:${PORT}/admin-api/;
        proxy_set_header   Host              \$host;
        proxy_set_header   X-Real-IP         \$remote_addr;
        proxy_set_header   X-Forwarded-For   \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto \$scheme;
        proxy_read_timeout 300s;
    }

    # 后端业务 api
    location /api/ {
        proxy_pass         http://127.0.0.1:${PORT}/api/;
        proxy_set_header   Host              \$host;
        proxy_set_header   X-Real-IP         \$remote_addr;
        proxy_set_header   X-Forwarded-For   \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto \$scheme;
        proxy_read_timeout 300s;
    }

    # WebSocket
    location /infra/ws {
        proxy_pass         http://127.0.0.1:${PORT}/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade    \$http_upgrade;
        proxy_set_header   Connection "upgrade";
        proxy_set_header   Host       \$host;
        proxy_read_timeout 3600s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?|ttf|eot)\$ {
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }
}
NGINX_EOF

if nginx -t 2>&1; then
  nginx -s reload 2>/dev/null || systemctl reload nginx 2>/dev/null || true
  ok "Nginx 已重载"
else
  warn "Nginx 配置语法有误，请手动检查: nginx -t"
fi

# ══════════════════════════════════════════════════════════════
# 步骤 10 — systemd 开机自启
# ══════════════════════════════════════════════════════════════
title "步骤 10  配置 systemd"
JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/}java"

cat > /etc/systemd/system/deepay.service << UNIT
[Unit]
Description=Deepay Backend (Spring Boot)
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=${RUN}
ExecStart=${JAVA_BIN} ${JAVA_OPTS} -jar ${JAR} --spring.profiles.active=${PROFILE} --spring.config.additional-location=file:./config/
ExecStop=/bin/kill -15 \$MAINPID
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=append:${LOG}
StandardError=append:${LOG}

[Install]
WantedBy=multi-user.target
UNIT

systemctl daemon-reload
systemctl enable deepay 2>/dev/null && ok "deepay.service 已设为开机自启" || warn "systemd enable 失败（可忽略）"

# ══════════════════════════════════════════════════════════════
echo ""
echo -e "${G}${B}"
echo "  ╔══════════════════════════════════════════════════════════╗"
echo "  ║            🎉  全自动部署完成！                         ║"
echo "  ╠══════════════════════════════════════════════════════════╣"
printf "  ║  站点      http://%-38s║\n" "${DOMAIN}"
printf "  ║  后端端口  http://127.0.0.1:%-29s║\n" "${PORT}"
printf "  ║  数据库    %-44s║\n" "${DB}  用户: ${DB_USER}"
echo "  ╠══════════════════════════════════════════════════════════╣"
printf "  ║  后端日志  %-44s║\n" "tail -f ${LOG}"
printf "  ║  日常更新  %-44s║\n" "git pull && bash script/shell/deepay-deploy.sh"
echo "  ╠══════════════════════════════════════════════════════════╣"
echo "  ║  💡 HTTPS：宝塔面板 → 网站 → ${DOMAIN} → SSL            ║"
echo "  ╚══════════════════════════════════════════════════════════╝"
echo -e "${N}"
