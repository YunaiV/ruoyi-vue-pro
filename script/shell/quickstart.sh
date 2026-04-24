#!/bin/bash
# =============================================================
#  Deepay 全自动一键部署脚本
#
#  服务器上执行（只需复制这一行）：
#    bash /www/wwwroot/deepay.srl/script/shell/quickstart.sh
#
#  脚本会自动完成：
#    ① 自动识别宝塔 JDK17，无需手动配置 JAVA_HOME
#    ② 检查 Java / Maven / Node / npm / MySQL / Redis / Nginx
#    ③ 配置 Maven 阿里云加速镜像（仅首次）
#    ④ 创建 MySQL 数据库 + 账号
#    ⑤ 按顺序导入全部 SQL 文件（幂等，已存在的表自动跳过）
#    ⑥ 写入后端生产配置 application-prod.yml
#    ⑦ Maven 打包后端（-pl yudao-server -am，跳过测试）
#    ⑧ 停止旧后端进程 → 启动新进程 → 健康检查
#    ⑨ npm 安装依赖 → Vite 构建前端 → 部署到站点根目录
#    ⑩ 生成 单站点 Nginx 配置（deepay.srl）→ reload
#    ⑪ 注册 systemd 服务 → 设为开机自启
#
#  环境变量覆盖（全部可选，默认按单站点方案）：
#    PROJECT         工程根目录（默认 /www/wwwroot/deepay.srl）
#    FRONTEND_OUT    前端部署目录（默认 $PROJECT）
#    DOMAIN          站点域名（默认 deepay.srl）
#    NGINX_CONF      Nginx 配置文件路径
#    DB / DB_USER / DB_PASS  数据库账号
#    PORT            后端端口（默认 48080）
#    PROFILE         Spring profile（默认 prod）
#    JAVA_HOME       手动指定 JDK 目录（默认自动检测宝塔 JDK17）
#    NONINTERACTIVE  设为 1 时禁止交互式提示，密码缺失直接报错
# =============================================================
set -euo pipefail

# ╔══════════════════════════════════════════════════════════╗
# ║   全局配置（环境变量优先，其次下方默认值）               ║
# ╚══════════════════════════════════════════════════════════╝
PROJECT="${PROJECT:-/www/wwwroot/deepay.srl}"
RUN=$PROJECT/run/backend
CFG=$RUN/config
JAR=$RUN/app.jar
LOG=$RUN/logs/app.log
PID=$RUN/deepay.pid
PORT="${PORT:-48080}"
PROFILE="${PROFILE:-prod}"

FRONTEND_SRC=$PROJECT/yudao-ui-deepay
# 前端部署到独立目录，Nginx 单独 server block 指向此处
FRONTEND_OUT="${FRONTEND_OUT:-/www/wwwroot/admin}"

# 单域名配置（不再生成 admin. / api. 子域名）
DOMAIN="${DOMAIN:-deepay.srl}"
NGINX_CONF="${NGINX_CONF:-/www/server/panel/vhost/nginx/deepay.conf}"

DB="${DB:-deepay}"
DB_USER="${DB_USER:-deepay}"
DB_PASS="${DB_PASS:-SRx3ETXjNWRKofo51jvc}"

NONINTERACTIVE="${NONINTERACTIVE:-0}"

JAVA_OPTS="-server -Xms512m -Xmx512m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${RUN}/heapDump"

# SQL 文件导入顺序（已合并为 3 个文件，幂等安全，MySQL 5.7 / 8.0 均兼容）
SQL_FILES=(
  "ruoyi-vue-pro.sql"   # 若依框架系统表
  "quartz.sql"          # 定时任务表
  "deepay.sql"          # Deepay 全量业务表（含所有迁移，合并自原 10 个文件）
)
# ═══════════════════════════════════════════════════════════

# ── 颜色 ─────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; B='\033[1m'; N='\033[0m'
ok()    { echo -e "${G}  ✓  $*${N}"; }
info()  { echo -e "     $*"; }
warn()  { echo -e "${Y}  ⚠  $*${N}"; }
error() { echo -e "${R}  ✗  $*${N}"; exit 1; }
title() { echo -e "\n${C}${B}══ $* ══${N}"; }

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
         全 自 动 一 键 部 署 脚 本
EOF
}
banner

# ══════════════════════════════════════════════════════════
# 步骤 0 — 自动检测宝塔 JDK17，修复"未找到 java"
# ══════════════════════════════════════════════════════════
title "步骤 0  自动检测 Java（宝塔 JDK17）"

_setup_java() {
  # 优先：外部传入 JAVA_HOME
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
    ok "使用指定 JAVA_HOME: $JAVA_HOME"
    return 0
  fi

  # 其次：宝塔安装目录 /www/server/java — 优先选 jdk-17
  if [ -d /www/server/java ]; then
    local _jdk=""
    # 先找 jdk-17 前缀
    _jdk=$(find /www/server/java -maxdepth 2 -name "java" -path "*/jdk-17*/bin/java" 2>/dev/null \
           | sort -rV | head -1 || true)
    # 退而求其次：任意 jdk
    if [ -z "$_jdk" ]; then
      _jdk=$(find /www/server/java -maxdepth 2 -name "java" -path "*/bin/java" 2>/dev/null \
             | sort -rV | head -1 || true)
    fi
    if [ -n "$_jdk" ] && [ -x "$_jdk" ]; then
      export JAVA_HOME
      JAVA_HOME=$(dirname "$(dirname "$_jdk")")
      export PATH="$JAVA_HOME/bin:$PATH"
      ok "自动检测到宝塔 JDK: $JAVA_HOME"
      return 0
    fi
  fi

  # 最后：系统 PATH
  if command -v java &>/dev/null; then
    ok "使用系统 PATH 中的 java: $(command -v java)"
    return 0
  fi

  error "未找到 Java！请在宝塔软件商店安装 JDK17 后重试，或设置 JAVA_HOME 环境变量"
}
_setup_java

# ══════════════════════════════════════════════════════════
# 步骤 1 — 自动安装缺失依赖
# ══════════════════════════════════════════════════════════
title "步骤 1  检查并安装依赖"

# 检测包管理器
if command -v apt-get &>/dev/null; then
  PKG_MGR="apt"
  export DEBIAN_FRONTEND=noninteractive
elif command -v yum &>/dev/null; then
  PKG_MGR="yum"
elif command -v dnf &>/dev/null; then
  PKG_MGR="dnf"
else
  PKG_MGR="unknown"
fi

_pkg_install() {
  local pkg="$1"
  info "正在安装 ${pkg} ..."
  case "$PKG_MGR" in
    apt) apt-get install -y -q "$pkg" 2>&1 | tail -2 ;;
    yum) yum install -y "$pkg" 2>&1 | tail -2 ;;
    dnf) dnf install -y "$pkg" 2>&1 | tail -2 ;;
    *)   warn "无法自动安装 ${pkg}，请手动安装后重试"; return 1 ;;
  esac
}

# 确保 Nginx 已安装并运行
if ! command -v nginx &>/dev/null; then
  _pkg_install nginx
fi
systemctl enable nginx 2>/dev/null || true
systemctl start  nginx 2>/dev/null || service nginx start 2>/dev/null || true
ok "Nginx : $(nginx -v 2>&1)"

# 确保 MySQL 已安装并运行
if ! command -v mysql &>/dev/null; then
  if [ "$PKG_MGR" = "apt" ]; then
    _pkg_install mysql-server
  else
    _pkg_install mysql-community-server || _pkg_install mariadb-server
  fi
fi
systemctl enable mysql  2>/dev/null || systemctl enable mysqld  2>/dev/null || true
systemctl start  mysql  2>/dev/null || systemctl start  mysqld  2>/dev/null \
  || service mysql start 2>/dev/null || true
ok "MySQL : $(mysql --version 2>&1 | head -1)"

# 确保 Maven 已安装
if ! command -v mvn &>/dev/null; then
  _pkg_install maven
fi
ok "Maven : $(mvn -version 2>&1 | head -1)"

# 确保 Node / npm 已安装（需要 16+）
if ! command -v node &>/dev/null; then
  if [ "$PKG_MGR" = "apt" ]; then
    # NodeSource LTS 20
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - 2>&1 | tail -3
    _pkg_install nodejs
  else
    _pkg_install nodejs npm || true
  fi
fi
ok "Node  : $(node -v)  npm: $(npm -v)"

# Redis 检查（不强制安装，宝塔一般已装）
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

# ── 自动获取 MySQL root 访问权限（支持全新安装） ──────────
BT_MYSQL_PWD=""
BT_CFG=/www/server/panel/config/config.json

# 方式 1：宝塔 config.json
if [ -f "$BT_CFG" ]; then
  BT_MYSQL_PWD=$(python3 -c \
    "import json; d=json.load(open('$BT_CFG')); print(d.get('mysql_root',''))" \
    2>/dev/null || true)
  [ -n "$BT_MYSQL_PWD" ] && ok "从宝塔配置读取 MySQL root 密码"
fi

# 方式 2：脚本上次运行保存的密码文件
if [ -z "$BT_MYSQL_PWD" ] && [ -f /root/.deepay_mysql_root ]; then
  BT_MYSQL_PWD=$(cat /root/.deepay_mysql_root)
  [ -n "$BT_MYSQL_PWD" ] && ok "从 /root/.deepay_mysql_root 读取 root 密码"
fi

# 方式 3：全新安装 — 尝试空密码连接
if [ -z "$BT_MYSQL_PWD" ]; then
  if mysql -u root --connect-timeout=5 -e "SELECT 1;" &>/dev/null 2>&1; then
    ok "全新 MySQL 安装，root 当前无密码"
    # 生成随机 root 密码并切换认证插件（兼容 Ubuntu socket auth）
    _ROOT_PWD=$(openssl rand -base64 18 | tr -dc 'a-zA-Z0-9' | head -c 20)
    mysql -u root -e \
      "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${_ROOT_PWD}'; FLUSH PRIVILEGES;" \
      2>/dev/null \
      || mysql -u root -e \
      "UPDATE mysql.user SET authentication_string=PASSWORD('${_ROOT_PWD}'), plugin='mysql_native_password' WHERE User='root'; FLUSH PRIVILEGES;" \
      2>/dev/null || true
    BT_MYSQL_PWD="$_ROOT_PWD"
    echo "$BT_MYSQL_PWD" > /root/.deepay_mysql_root
    chmod 600 /root/.deepay_mysql_root
    ok "已自动设置 MySQL root 密码并保存至 /root/.deepay_mysql_root"
  fi
fi

# 方式 4：Ubuntu socket auth — 用 sudo mysql 初始化
if [ -z "$BT_MYSQL_PWD" ]; then
  if sudo mysql -u root --connect-timeout=5 -e "SELECT 1;" &>/dev/null 2>&1; then
    ok "检测到 Ubuntu socket 认证模式，正在切换为密码认证..."
    _ROOT_PWD=$(openssl rand -base64 18 | tr -dc 'a-zA-Z0-9' | head -c 20)
    sudo mysql -u root -e \
      "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${_ROOT_PWD}'; FLUSH PRIVILEGES;" \
      2>/dev/null || true
    BT_MYSQL_PWD="$_ROOT_PWD"
    echo "$BT_MYSQL_PWD" > /root/.deepay_mysql_root
    chmod 600 /root/.deepay_mysql_root
    ok "已切换为密码认证，root 密码保存至 /root/.deepay_mysql_root"
  fi
fi

# 方式 5：交互式输入（最后兜底）
if [ -z "$BT_MYSQL_PWD" ]; then
  if [ "${NONINTERACTIVE}" = "1" ]; then
    error "无法自动获取 MySQL root 密码，请设置环境变量 NONINTERACTIVE=0 后重试"
  fi
  echo -n "  请输入 MySQL root 密码: "
  read -rs BT_MYSQL_PWD; echo ""
fi

MYSQL_CMD="mysql -u root -p${BT_MYSQL_PWD} --connect-timeout=5"
$MYSQL_CMD -e "SELECT 1;" &>/dev/null || error "MySQL root 密码不正确或 MySQL 未运行"

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
  autoconfigure:
    exclude:
      # Druid 独立配置，不需要自动装配
      - com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
      # Quartz 表须手动建好，不自动初始化
      - org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration
      # AI 向量库按需手动创建
      - org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration
      - org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration
      # 微信公众号/小程序 appid 仅在 application-local.yaml 配置；
      # prod 不使用微信功能，禁用避免 appid=null 启动崩溃
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

  # ⚠️ Spring Boot 2.7.x：Redis 前缀是 spring.redis（非 spring.data.redis）
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000ms
    # password:   # 若宝塔 Redis 设了密码，取消注释并填入

yudao:
  web:
    admin-ui:
      url: https://${DOMAIN}
YAML
ok "已写入 $CFG/application-prod.yml"

# ══════════════════════════════════════════════════════════
# 步骤 5 — 获取后端 JAR（优先从 GitHub Release 下载，失败则本地 Maven 打包）
# ══════════════════════════════════════════════════════════
title "步骤 5  获取后端 JAR"
cd "$PROJECT"
BUILD_LOG=$RUN/backup/build-${DATE}.log

RELEASE_URL="https://github.com/deepveloce-dot/ruoyi-vue-pro/releases/download/latest/yudao-server.jar"
DOWNLOAD_JAR=$RUN/backup/yudao-server-${DATE}.jar

info "尝试从 GitHub Release 下载预构建 JAR ..."
if curl -fsSL --retry 3 --connect-timeout 15 -o "$DOWNLOAD_JAR" "$RELEASE_URL" 2>>"$BUILD_LOG"; then
  NEW_JAR="$DOWNLOAD_JAR"
  ok "下载成功: $NEW_JAR"
else
  warn "下载失败，回退到本地 Maven 打包（需要几分钟，请耐心等待）..."

  # 第一段：清除失败缓存，预安装 yudao-dependencies BOM
  # yudao-dependencies 从未发布到远程仓库，-pl yudao-server -am 不含它，
  # Maven model-building 阶段会报 "Non-resolvable import POM"，必须先本地安装。
  # 显式传 -Drevision 确保安装版本与根 pom 一致（yudao-dependencies/pom.xml 自带 revision 可能与根不同）
  REVISION=$(grep -oP '(?<=<revision>)[^<]+' "$PROJECT/pom.xml" | head -1)
  rm -rf ~/.m2/repository/cn/iocoder/boot/yudao-dependencies/ 2>/dev/null || true
  mvn install -f "$PROJECT/yudao-dependencies/pom.xml" \
    -Drevision="$REVISION" \
    -DskipTests --no-transfer-progress -q \
    2>&1 | tee -a "$BUILD_LOG"
  [ "${PIPESTATUS[0]}" -eq 0 ] || error "yudao-dependencies 预安装失败！查看: $BUILD_LOG"
  ok "yudao-dependencies BOM 已就绪"

  # 第二段：构建主项目（BOM 已在本地 .m2）
  mvn clean package -DskipTests --batch-mode -pl yudao-server -am \
    2>&1 | tee "$BUILD_LOG" | grep -E "BUILD|ERROR|yudao-server.*jar" || true

  NEW_JAR=$(find "$PROJECT/yudao-server/target" -maxdepth 1 -name "*.jar" \
            ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1)
  [ -f "$NEW_JAR" ] || error "打包失败！查看完整日志: $BUILD_LOG"
  ok "打包成功: $NEW_JAR"
fi

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
# 注意：本项目未暴露 /actuator/health，使用根路径 / 做存活检测
HEALTH=http://127.0.0.1:${PORT}/
STATUS=000
for i in $(seq 1 120); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH" 2>/dev/null || echo "000")
  [ "$STATUS" != "000" ] && break
  printf "."
  sleep 1
done
echo ""
[ "$STATUS" != "000" ] && ok "后端已就绪 ✓  HTTP $STATUS" \
  || warn "120s 后仍无响应，可能还在启动中。查看: tail -f $LOG"

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
# 步骤 9 — 写 Nginx 配置（单站点：deepay.srl）
# ══════════════════════════════════════════════════════════
title "步骤 9  配置 Nginx（单站点 ${DOMAIN}）"

mkdir -p "$(dirname "$NGINX_CONF")"

# 备份旧配置
if [ -f "$NGINX_CONF" ]; then
  cp -f "$NGINX_CONF" "${NGINX_CONF}.bak.${DATE}"
  ok "旧 Nginx 配置已备份 → ${NGINX_CONF}.bak.${DATE}"
fi

# 写单站点配置（使用 shell 变量替换 DOMAIN / FRONTEND_OUT / PORT）
# Nginx 内部变量用 \$ 转义
cat > "$NGINX_CONF" << NGINX_EOF
# ============================================================
#  Deepay — Nginx 虚拟主机配置（单站点）
#
#  ${DOMAIN}  → Vue 前端（${FRONTEND_OUT}）
#               /api/       反代 → 127.0.0.1:${PORT}
#               /admin-api/ 反代 → 127.0.0.1:${PORT}
#               /infra/ws   WebSocket
#
#  自动生成于 $(date)
#  HTTPS：宝塔面板 → 网站 → ${DOMAIN} → SSL → Let's Encrypt
# ============================================================

server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN} www.${DOMAIN};

    root ${FRONTEND_OUT};
    index index.html;

    client_max_body_size 32m;

    # 前端 SPA（Vue Router history 模式）
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
        proxy_connect_timeout 60s;
        proxy_read_timeout    300s;
        proxy_send_timeout    300s;
    }

    # 后端 api（业务接口）
    location /api/ {
        proxy_pass         http://127.0.0.1:${PORT}/api/;
        proxy_set_header   Host              \$host;
        proxy_set_header   X-Real-IP         \$remote_addr;
        proxy_set_header   X-Forwarded-For   \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout    300s;
        proxy_send_timeout    300s;
    }

    # WebSocket（实时推送）
    location /infra/ws {
        proxy_pass         http://127.0.0.1:${PORT}/infra/ws;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade    \$http_upgrade;
        proxy_set_header   Connection "upgrade";
        proxy_set_header   Host       \$host;
        proxy_read_timeout 3600s;
    }

    # Swagger（调试用）
    location /swagger-ui {
        proxy_pass http://127.0.0.1:${PORT}/swagger-ui;
        proxy_set_header Host \$host;
    }
    location /v3/api-docs {
        proxy_pass http://127.0.0.1:${PORT}/v3/api-docs;
        proxy_set_header Host \$host;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?|ttf|eot)\$ {
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }
}
NGINX_EOF

ok "Nginx 配置已写入 $NGINX_CONF"
if nginx -t 2>&1; then
  systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null \
    || warn "Nginx reload 失败，请宝塔面板手动重载"
  ok "Nginx 已重载"
else
  warn "Nginx 配置语法有误，请检查 $NGINX_CONF"
fi

# ══════════════════════════════════════════════════════════
# 步骤 10 — systemd 开机自启
# ══════════════════════════════════════════════════════════
title "步骤 10  配置 systemd 开机自启"

# 使用步骤 0 检测到的 java（JAVA_HOME 已 export）
JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/}java"

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
systemctl restart deepay 2>/dev/null && ok "deepay.service 已启动" || warn "systemd start 失败，请查看: journalctl -u deepay -n 30"

# ══════════════════════════════════════════════════════════
# 完成汇总
# ══════════════════════════════════════════════════════════
echo ""
echo -e "${G}${B}"
echo "  ╔══════════════════════════════════════════════════════════╗"
echo "  ║           🎉  全自动部署完成！                          ║"
echo "  ╠══════════════════════════════════════════════════════════╣"
echo "  ║  站点    http://${DOMAIN}"
echo "  ║  API     http://${DOMAIN}/api/"
echo "  ║  Swagger http://${DOMAIN}/swagger-ui"
echo "  ╠══════════════════════════════════════════════════════════╣"
echo "  ║  数据库  ${DB}  用户: ${DB_USER}"
echo "  ║  后端日志  tail -f ${LOG}"
echo "  ║  重新部署  bash ${PROJECT}/script/shell/quickstart.sh"
echo "  ╠══════════════════════════════════════════════════════════╣"
echo "  ║  💡 开启 HTTPS：                                        ║"
echo "  ║     宝塔面板 → 网站 → ${DOMAIN} → SSL → Let's Encrypt  ║"
echo "  ╚══════════════════════════════════════════════════════════╝"
echo -e "${N}"

