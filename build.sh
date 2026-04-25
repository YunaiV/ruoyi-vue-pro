#!/usr/bin/env bash
# =============================================================
#  Deepay 一键构建 & 部署脚本  v2.0
#
#  用法：
#    bash build.sh              # 并行构建全部，自动部署到 Nginx
#    bash build.sh admin        # 仅构建 PWA 管理端
#    bash build.sh app          # 仅构建 uni-app H5
#    bash build.sh all          # 同不带参数
#    bash build.sh --no-cache   # 强制忽略缓存重新构建
#    bash build.sh --no-deploy  # 构建但不部署
#    bash build.sh --help       # 显示帮助
#
#  前置条件：Node 18 ~ 24（推荐 20 LTS），npm ≥ 9
# =============================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CACHE_DIR="$ROOT/.build-cache"
LOG_DIR="$ROOT/.build-logs"
DEPLOY_PWA="/www/wwwroot/deepay.srl"
DEPLOY_H5="/www/wwwroot/deepay.srl/app"
JOBS=2          # 并行任务数
NO_CACHE=false
NO_DEPLOY=false
TARGET="all"
BUILD_START=$(date +%s)

# ── 彩色输出 ──────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'
C='\033[0;36m'; B='\033[1m'; N='\033[0m'
info()  { echo -e "${G}[INFO ]${N} $*"; }
warn()  { echo -e "${Y}[WARN ]${N} $*"; }
step()  { echo -e "\n${C}${B}▶ $*${N}"; }
ok()    { echo -e "${G}${B}  ✔  $*${N}"; }
fail()  { echo -e "${R}${B}  ✘  $*${N}"; }
err()   { fail "$*"; exit 1; }
hr()    { echo -e "${C}$(printf '─%.0s' {1..60})${N}"; }
elapsed() { echo $(( $(date +%s) - $1 ))s; }

# ── 参数解析 ──────────────────────────────────────────────────
for arg in "$@"; do
  case "$arg" in
    --no-cache)  NO_CACHE=true ;;
    --no-deploy) NO_DEPLOY=true ;;
    --help|-h)
      sed -n '2,12p' "$0" | sed 's/^#  \?//'
      exit 0 ;;
    admin|app|all) TARGET="$arg" ;;
    *) err "未知参数: $arg  →  bash build.sh --help 查看用法" ;;
  esac
done

mkdir -p "$CACHE_DIR" "$LOG_DIR"

# ── 环境预检 ──────────────────────────────────────────────────
preflight() {
  hr
  info "Node : $(node -v 2>/dev/null || echo '未安装')"
  info "npm  : $(npm  -v 2>/dev/null || echo '未安装')"
  info "目标 : $TARGET  |  缓存: $( $NO_CACHE && echo 关闭 || echo 开启)  |  部署: $( $NO_DEPLOY && echo 关闭 || echo 开启)"
  hr

  command -v node >/dev/null 2>&1 || err "未找到 node，请安装 Node.js 18~20 LTS"

  local NODE_MAJOR
  NODE_MAJOR=$(node -v | sed 's/v\([0-9]*\).*/\1/')
  (( NODE_MAJOR >= 18 && NODE_MAJOR < 25 )) \
    || warn "Node 版本 $(node -v) 超出推荐范围 18~24，可能出现兼容问题"

  local DISK_FREE
  DISK_FREE=$(df -BM "$ROOT" | awk 'NR==2{gsub("M","",$4); print $4}')
  (( DISK_FREE >= 500 )) || warn "磁盘剩余空间不足 500 MB（当前 ${DISK_FREE}MB），构建可能失败"
}

# ── 哈希缓存：源文件无变化则跳过构建 ─────────────────────────
_cache_key() {
  # 对 src/ + package.json + vite.config.js 做 hash
  find "$1/src" "$1/package.json" "$1/vite.config.js" \
    -type f 2>/dev/null \
    | sort | xargs md5sum 2>/dev/null | md5sum | awk '{print $1}'
}

_cache_hit() {
  $NO_CACHE && return 1
  local name="$1" dir="$2"
  local key_file="$CACHE_DIR/${name}.hash"
  local current; current=$(_cache_key "$dir")
  [ -f "$key_file" ] && [ "$(cat "$key_file")" = "$current" ]
}

_cache_save() {
  local name="$1" dir="$2"
  _cache_key "$dir" > "$CACHE_DIR/${name}.hash"
}

# ── npm 安装（失败自动降级到 legacy-peer-deps）────────────────
_install() {
  if [ -f package-lock.json ]; then
    info "package-lock.json 存在 → npm ci"
    npm ci 2>&1 || {
      warn "npm ci 失败，尝试 npm ci --legacy-peer-deps"
      npm ci --legacy-peer-deps 2>&1
    }
  else
    info "未发现 lockfile → npm install"
    npm install 2>&1 || {
      warn "npm install 失败，尝试 --legacy-peer-deps"
      npm install --legacy-peer-deps 2>&1
    }
  fi
}

# ── 单项目构建函数 ────────────────────────────────────────────
_build_project() {
  local name="$1"   # 标识符
  local dir="$2"    # 项目目录
  local cmd="$3"    # 构建命令
  local dist="$4"   # 产物目录
  local log="$LOG_DIR/${name}.log"
  local t0; t0=$(date +%s)

  step "[$name]"
  cd "$dir"

  # 缓存命中 → 跳过
  if _cache_hit "$name" "$dir"; then
    ok "[$name] 源文件无变化，命中缓存，跳过构建 ⚡"
    return 0
  fi

  info "[$name] 安装依赖..."
  _install >> "$log" 2>&1 || { fail "[$name] 依赖安装失败，详见: $log"; return 1; }

  info "[$name] 执行构建..."
  npm run "$cmd" >> "$log" 2>&1 || { fail "[$name] 构建失败，详见: $log"; return 1; }

  _cache_save "$name" "$dir"
  ok "[$name] 构建完成 ($(elapsed $t0))  →  $dist"
}

# ── 部署到 Nginx 目录 ─────────────────────────────────────────
_deploy() {
  local src="$1" dst="$2" name="$3"
  $NO_DEPLOY && return 0
  [ -d "$src" ] || { warn "[$name] 产物目录不存在，跳过部署: $src"; return 0; }

  info "[$name] 部署 $src → $dst"
  mkdir -p "$dst"
  # 增量同步：只替换变更文件，保留其他目录
  rsync -a --delete "$src/" "$dst/" 2>/dev/null \
    || { warn "rsync 不可用，回退到 cp"; cp -rf "$src/." "$dst/"; }
  ok "[$name] 部署完成 → $dst"
}

# ── 并行构建入口 ──────────────────────────────────────────────
build_admin() {
  _build_project "pwa"  "$ROOT/yudao-ui-deepay"     "build"    "$ROOT/yudao-ui-deepay/dist"
}

build_app() {
  _build_project "h5"   "$ROOT/yudao-ui-deepay-app" "build:h5" "$ROOT/yudao-ui-deepay-app/dist/build/h5"
}

deploy_all() {
  _deploy "$ROOT/yudao-ui-deepay/dist"             "$DEPLOY_PWA" "pwa"
  _deploy "$ROOT/yudao-ui-deepay-app/dist/build/h5" "$DEPLOY_H5" "h5"
}

# ── 主流程 ────────────────────────────────────────────────────
preflight

FAILED=()

case "$TARGET" in
  admin)
    build_admin || FAILED+=("pwa")
    deploy_all
    ;;
  app)
    build_app || FAILED+=("h5")
    deploy_all
    ;;
  all)
    # 并行构建两个项目
    if (( JOBS >= 2 )); then
      info "并行构建（jobs=$JOBS）..."
      build_admin &  PID_ADMIN=$!
      build_app   &  PID_APP=$!
      wait $PID_ADMIN || FAILED+=("pwa")
      wait $PID_APP   || FAILED+=("h5")
    else
      build_admin || FAILED+=("pwa")
      build_app   || FAILED+=("h5")
    fi
    deploy_all
    ;;
esac

# ── 构建总结 ──────────────────────────────────────────────────
hr
if (( ${#FAILED[@]} == 0 )); then
  ok "全部完成 🎉  总耗时: $(elapsed $BUILD_START)"
  echo ""
  echo "  产物目录："
  echo "    PWA  →  $ROOT/yudao-ui-deepay/dist/"
  echo "    H5   →  $ROOT/yudao-ui-deepay-app/dist/build/h5/"
  $NO_DEPLOY || echo ""
  $NO_DEPLOY || echo "  已部署到："
  $NO_DEPLOY || echo "    PWA  →  $DEPLOY_PWA"
  $NO_DEPLOY || echo "    H5   →  $DEPLOY_H5"
else
  fail "以下项目构建失败：${FAILED[*]}"
  echo "  日志目录：$LOG_DIR"
  exit 1
fi
hr
