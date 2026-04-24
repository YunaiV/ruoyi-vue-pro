#!/bin/bash
# =============================================================
#  Deepay 一键完全配置 & 部署脚本
#
#  服务器上执行（只需复制这一行）：
#    bash /www/wwwroot/deepay.srl/script/shell/quickstart.sh
#
#  脚本会自动完成：
#    ① 检查 Java / Maven / Node / npm / MySQL / Redis / Nginx
#    ② 配置 Maven 阿里云加速镜像（仅首次）
#    ③ 创建 MySQL 数据库 + 账号
#    ④ 按顺序导入全部 SQL 文件（幂等，已存在的表自动跳过）
#    ⑤ 写入后端生产配置 application-prod.yml
#    ⑥ Maven 打包后端（-pl yudao-server -am，跳过测试）
#    ⑦ 停止旧后端进程 → 启动新进程 → 健康检查
#    ⑧ npm 安装依赖 → Vite 构建前端 → 部署到 Nginx 目录
#    ⑨ 生成 Nginx 虚拟主机配置 → reload
#    ⑩ 注册 systemd 服务 → 设为开机自启
# =============================================================
set -euo pipefail

# ╔══════════════════════════════════════════════════════════╗
# ║   全局配置（如需修改只改这里）                           ║
# ╚══════════════════════════════════════════════════════════╝
PROJECT=/www/wwwroot/deepay.srl
RUN=$PROJECT/run/backend
CFG=$RUN/config
JAR=$RUN/app.jar
LOG=$RUN/logs/app.log
PID=$RUN/deepay.pid
PORT=48080
PROFILE=prod

FRONTEND_SRC=$PROJECT/yudao-ui-deepay
FRONTEND_OUT=/www/wwwroot/admin          # 与宝塔站点根目录一致

DOMAIN_ADMIN=admin.deepay.srl            # 前端 + API 反代域名
NGINX_CONF=/www/server/panel/vhost/nginx/${DOMAIN_ADMIN}.conf

DB=sdsdsdas
DB_USER=sdsdsdas
DB_PASS=sdsdsdas

JAVA_OPTS="-server -Xms512m -Xmx512m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${RUN}/heapDump"

# SQL 文件导入顺序（基础表 → 增量迁移，必须保持此顺序）
SQL_FILES=(
  "ruoyi-vue-pro.sql"
  "quartz.sql"
  "deepay.sql"
  "deepay-phase67.sql"
  "deepay-phase10.sql"
  "deepay-feature-config.sql"
  "deepay-fx.sql"
  "deepay-selection-v2.sql"
  "deepay-v2-ima.sql"
  "deepay-v3-inventory.sql"
  "deepay-v3-payment.sql"
  "deepay-v3-product-fields.sql"
  "deepay-v4-menu.sql"
)
# ═══════════════════════════════════════════════════════════

# ── 颜色 ─────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; B='\033[1m'; N='\033[0m'
ok()    { echo -e "${G}  ✓  $*${N}"; }
info()  { echo -e "     $*"; }
warn()  { echo -e "${Y}  ⚠  $*${N}"; }
error() { echo -e "${R}  ✗  $*${N}"; exit 1; }
title() { echo -e "\n${C}${B}══ $* ══${N}"; }
need()  { command -v "$1" &>/dev/null || error "未找到 $1，请在宝塔软件商店安装后重试"; }

DATE=$(date '+%Y%m%d_%H%M%S')
mkdir -p "$RUN/logs" "$RUN/backup" "$CFG"

# ══════════════════════════════════════════════════════════
banner() {
cat << 'EOF'

  ██████╗ ███████╗███████╗██████╗  █████╗ ██╗   ██╗
  ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗╚██╗ ██╔╝
  ██║  ██║█████╗  █████╗  ██████╔╝███████║ ╚████╔╝
  ██║  ██║██╔══╝  ██╔══╝  ██╔═══╝ ██╔══██║  ╚██╔╝
  ██████╔╝███████╗███████╗██║     ██║  ██║   ██║
  ╚═════╝ ╚══════╝╚══════╝╚═╝     ╚═╝  ╚═╝   ╚═╝
         一 键 完 全 配 置 脚 本
EOF
}
banner

# ══════════════════════════════════════════════════════════
# 步骤 1 — 检查依赖
# ══════════════════════════════════════════════════════════
title "步骤 1  检查环境"
need java; need mvn; need node; need npm; need mysql; need nginx

ok "Java  : $(java  -version 2>&1 | head -1)"
ok "Maven : $(mvn   -version 2>&1 | head -1)"
ok "Node  : $(node  -v)"
ok "npm   : $(npm   -v)"

if command -v redis-cli &>/dev/null; then
  redis-cli ping &>/dev/null && ok "Redis : 运行中" || warn "Redis 未响应，请检查宝塔 Redis 服务"
else
  warn "redis-cli 未找到，请确认宝塔 Redis 已安装并运行"
fi

# ══════════════════════════════════════════════════════════
# 步骤 2 — 配置 Maven 阿里云加速镜像（仅首次）
# ══════════════════════════════════════════════════════════
title "步骤 2  配置 Maven 阿里云镜像"

MVN_HOME=$(mvn -version 2>&1 | grep -oP '(?<=Maven home: ).*' || true)
# 优先用 ~/.m2/settings.xml，否则写到 Maven 安装目录
M2_SETTINGS="${HOME}/.m2/settings.xml"
mkdir -p "${HOME}/.m2"

if grep -q "aliyun" "$M2_SETTINGS" 2>/dev/null; then
  ok "Maven 阿里云镜像已配置，跳过"
else
  cat > "$M2_SETTINGS" << 'MAVEN_XML'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
            https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
MAVEN_XML
  ok "Maven 阿里云镜像已写入 $M2_SETTINGS"
fi

# ══════════════════════════════════════════════════════════
# 步骤 3 — 数据库：创建账号 + 按顺序导入全部 SQL
# ══════════════════════════════════════════════════════════
title "步骤 3  创建数据库 & 导入全部 SQL"

# 从宝塔读取 MySQL root 密码
BT_MYSQL_PWD=""
BT_CFG=/www/server/panel/config/config.json
if [ -f "$BT_CFG" ]; then
  BT_MYSQL_PWD=$(python3 -c "import json,sys; d=json.load(open('$BT_CFG')); print(d.get('mysql_root',''))" 2>/dev/null || true)
fi

if [ -z "$BT_MYSQL_PWD" ]; then
  echo -n "  请输入 MySQL root 密码（宝塔面板 → 数据库 → root密码）: "
  read -rs BT_MYSQL_PWD; echo ""
fi

MYSQL_CMD="mysql -u root -p${BT_MYSQL_PWD} --connect-timeout=5"
$MYSQL_CMD -e "SELECT 1;" &>/dev/null || error "MySQL root 密码不对或 MySQL 未运行"

# 创建数据库和账号（幂等）
$MYSQL_CMD <<SQL 2>/dev/null || true
CREATE DATABASE IF NOT EXISTS \`${DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON \`${DB}\`.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL
ok "数据库 ${DB} 和账号 ${DB_USER} 已就绪"

# ── 按顺序导入所有 SQL 文件（幂等：用 IF NOT EXISTS / INSERT IGNORE） ──
SQL_DIR="$PROJECT/sql/mysql"
IMPORT_ERRORS=0
for SQL_FILE in "${SQL_FILES[@]}"; do
  FULL_PATH="$SQL_DIR/$SQL_FILE"
  if [ ! -f "$FULL_PATH" ]; then
    warn "SQL 文件不存在，跳过: $SQL_FILE"
    continue
  fi
  info "导入 $SQL_FILE ..."
  if $MYSQL_CMD "$DB" < "$FULL_PATH" 2>/tmp/sql_err_$$.txt; then
    ok "$SQL_FILE 导入完成"
  else
    # 常见的"已存在"错误可忽略，其他错误才告警
    if grep -qiE "already exists|Duplicate entry|duplicate key" /tmp/sql_err_$$.txt 2>/dev/null; then
      ok "$SQL_FILE（部分表/数据已存在，已跳过重复项）"
    else
      warn "$SQL_FILE 导入时有警告（可能部分已存在）:"
      head -5 /tmp/sql_err_$$.txt || true
      IMPORT_ERRORS=$((IMPORT_ERRORS + 1))
    fi
  fi
  rm -f /tmp/sql_err_$$.txt
done

if [ "$IMPORT_ERRORS" -eq 0 ]; then
  ok "全部 ${#SQL_FILES[@]} 个 SQL 文件导入完成"
else
  warn "${IMPORT_ERRORS} 个文件导入时有异常，请查看上方日志后手动确认"
fi

# ══════════════════════════════════════════════════════════
# 步骤 4 — 写后端生产配置
# ══════════════════════════════════════════════════════════
title "步骤 4  写后端生产配置"

cat > "$CFG/application-prod.yml" << YAML
server:
  port: ${PORT}

spring:
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

  # ⚠️ Spring Boot 2.7.x：Redis 前缀是 spring.redis（非 spring.data.redis）
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000ms
    # password:   # 若宝塔 Redis 设了密码，取消注释并填入
YAML
ok "已写入 $CFG/application-prod.yml"

# ══════════════════════════════════════════════════════════
# 步骤 5 — Maven 打包后端
# ══════════════════════════════════════════════════════════
title "步骤 5  Maven 打包后端（需要几分钟，请耐心等待）"
cd "$PROJECT"
BUILD_LOG=$RUN/backup/build-${DATE}.log
mvn clean package -DskipTests --batch-mode -pl yudao-server -am \
  2>&1 | tee "$BUILD_LOG" | grep -E "BUILD|ERROR|yudao-server.*jar" || true

NEW_JAR=$(find "$PROJECT/yudao-server/target" -maxdepth 1 -name "*.jar" \
          ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1)
[ -f "$NEW_JAR" ] || error "打包失败！查看完整日志: $BUILD_LOG"
ok "打包成功: $NEW_JAR"

# ══════════════════════════════════════════════════════════
# 步骤 6 — 停止旧后端 & 启动新后端
# ══════════════════════════════════════════════════════════
title "步骤 6  停止旧进程 → 启动新后端"

if [ -f "$PID" ]; then
  OLD=$(cat "$PID")
  if kill -0 "$OLD" 2>/dev/null; then
    kill -15 "$OLD" 2>/dev/null || true
    for i in $(seq 1 20); do kill -0 "$OLD" 2>/dev/null || break; sleep 1; printf "."; done
    echo ""
    kill -0 "$OLD" 2>/dev/null && { kill -9 "$OLD" 2>/dev/null || true; warn "已强制 kill -9"; }
    ok "旧进程已停止"
  fi
  rm -f "$PID"
fi
LEFTOVER=$(ss -lntp 2>/dev/null | grep ":${PORT}" | grep -oP 'pid=\K[0-9]+' | head -1 || true)
[ -n "$LEFTOVER" ] && { kill -9 "$LEFTOVER" 2>/dev/null || true; warn "端口 ${PORT} 残留进程已清理"; }

[ -f "$JAR" ] && cp -f "$JAR" "$RUN/backup/app-${DATE}.jar"
cp -f "$NEW_JAR" "$JAR"

cd "$RUN"
# shellcheck disable=SC2086
nohup java $JAVA_OPTS \
  -jar "$JAR" \
  --spring.profiles.active=$PROFILE \
  --spring.config.additional-location=file:./config/ \
  >> "$LOG" 2>&1 &
echo $! > "$PID"
ok "后端已启动  PID=$(cat "$PID")  日志→ $LOG"

info "等待后端就绪（最长 120 秒）..."
HEALTH=http://127.0.0.1:${PORT}/actuator/health
STATUS=000
for i in $(seq 1 120); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH" 2>/dev/null || echo "000")
  [ "$STATUS" = "200" ] && break
  printf "."
  sleep 1
done
echo ""
[ "$STATUS" = "200" ] && ok "后端健康检查通过 ✓" \
  || warn "120s 后仍未就绪（状态 $STATUS），可能还在启动中。查看: tail -f $LOG"

# ══════════════════════════════════════════════════════════
# 步骤 7 — 构建 & 部署前端
# ══════════════════════════════════════════════════════════
title "步骤 7  构建前端（需要几分钟）"
cd "$FRONTEND_SRC"
npm install --prefer-offline 2>&1 | tail -3
npm run build 2>&1 | tail -6
[ -d "$FRONTEND_SRC/dist" ] || error "前端构建失败，未找到 dist 目录"
ok "前端构建完成"

title "步骤 8  部署前端 → $FRONTEND_OUT"
if [ -d "$FRONTEND_OUT" ] && [ "$(ls -A "$FRONTEND_OUT" 2>/dev/null)" ]; then
  tar -czf "$RUN/backup/frontend-${DATE}.tar.gz" -C "$FRONTEND_OUT" . 2>/dev/null \
    && ok "旧前端已备份" || true
fi
mkdir -p "$FRONTEND_OUT"
rm -rf "${FRONTEND_OUT:?}"/*
cp -r "$FRONTEND_SRC/dist"/. "$FRONTEND_OUT/"
ok "前端已部署 → $FRONTEND_OUT"

# ══════════════════════════════════════════════════════════
# 步骤 9 — 写 Nginx 配置
# ══════════════════════════════════════════════════════════
title "步骤 9  配置 Nginx"

mkdir -p "$(dirname "$NGINX_CONF")"

cat > "$NGINX_CONF" << NGINX
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN_ADMIN};

    # 申请好 SSL 证书后，取消下一行注释并注释掉下面的 root/location 块
    # return 301 https://\$host\$request_uri;

    root ${FRONTEND_OUT};
    index index.html;

    # ── 前端 SPA（Vue Router history 模式）────────────────
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # ── 后端 API 反代（/api/ 转发给 Spring Boot）────────
    location /api/ {
        proxy_pass         http://127.0.0.1:${PORT}/api/;
        proxy_set_header   Host              \$host;
        proxy_set_header   X-Real-IP         \$remote_addr;
        proxy_set_header   X-Forwarded-For   \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout    300s;
        proxy_send_timeout    300s;
        client_max_body_size  32m;
    }

    # ── admin-api 反代 ───────────────────────────────────
    location /admin-api/ {
        proxy_pass         http://127.0.0.1:${PORT}/admin-api/;
        proxy_set_header   Host              \$host;
        proxy_set_header   X-Real-IP         \$remote_addr;
        proxy_set_header   X-Forwarded-For   \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout    300s;
        proxy_send_timeout    300s;
        client_max_body_size  32m;
    }

    # ── WebSocket（实时推送）──────────────────────────────
    location /infra/ws {
        proxy_pass         http://127.0.0.1:${PORT}/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade    \$http_upgrade;
        proxy_set_header   Connection "upgrade";
        proxy_set_header   Host       \$host;
        proxy_read_timeout 3600s;
    }

    # ── Swagger（调试用，上线后可删除）──────────────────
    location /swagger-ui {
        proxy_pass http://127.0.0.1:${PORT}/swagger-ui;
        proxy_set_header Host \$host;
    }
    location /v3/api-docs {
        proxy_pass http://127.0.0.1:${PORT}/v3/api-docs;
        proxy_set_header Host \$host;
    }

    # ── 静态资源缓存 ─────────────────────────────────────
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?|ttf|eot)\$ {
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }
}
NGINX

ok "Nginx 配置已写入 $NGINX_CONF"
if nginx -t 2>/dev/null; then
  systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null \
    || warn "Nginx reload 失败，请宝塔面板手动重载"
  ok "Nginx 已重载"
else
  warn "Nginx 配置有误，请检查 $NGINX_CONF"
fi

# ══════════════════════════════════════════════════════════
# 步骤 10 — systemd 开机自启
# ══════════════════════════════════════════════════════════
title "步骤 10  配置 systemd 开机自启"

JAVA_BIN=$(command -v java)

cat > /etc/systemd/system/deepay.service << UNIT
[Unit]
Description=Deepay Backend (Spring Boot)
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=${RUN}
ExecStart=${JAVA_BIN} ${JAVA_OPTS} \\
    -jar ${JAR} \\
    --spring.profiles.active=${PROFILE} \\
    --spring.config.additional-location=file:./config/
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
systemctl enable deepay 2>/dev/null && ok "deepay.service 已设为开机自启" || warn "systemd enable 失败，可忽略"

# ══════════════════════════════════════════════════════════
# 完成汇总
# ══════════════════════════════════════════════════════════
echo ""
echo -e "${G}${B}"
echo "  ╔══════════════════════════════════════════════════════╗"
echo "  ║           🎉  一键完全配置完成！                    ║"
echo "  ╠══════════════════════════════════════════════════════╣"
echo "  ║  前端地址   http://${DOMAIN_ADMIN}          ║"
echo "  ║  API 地址   http://${DOMAIN_ADMIN}/api/     ║"
echo "  ║  Swagger    http://${DOMAIN_ADMIN}/swagger-ui║"
echo "  ╠══════════════════════════════════════════════════════╣"
echo "  ║  后端日志   tail -f ${LOG}"
echo "  ║  重新部署   bash ${PROJECT}/script/shell/deepay-deploy.sh"
echo "  ║  只更新后端 bash ${PROJECT}/script/shell/deepay-deploy.sh backend"
echo "  ║  只更新前端 bash ${PROJECT}/script/shell/deepay-deploy.sh frontend"
echo "  ╠══════════════════════════════════════════════════════╣"
echo "  ║  💡 如要开启 HTTPS：                                ║"
echo "  ║     宝塔面板 → 网站 → ${DOMAIN_ADMIN}     ║"
echo "  ║     → SSL → Let's Encrypt → 申请证书               ║"
echo "  ╚══════════════════════════════════════════════════════╝"
echo -e "${N}"

