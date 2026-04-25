#!/usr/bin/env bash
# =============================================================
#  一键构建脚本 — 构建 Deepay 所有前端项目
#
#  用法（在项目根目录执行）：
#    bash build.sh          # 构建全部（默认）
#    bash build.sh admin    # 仅构建 yudao-ui-deepay  (Vue3 PWA)
#    bash build.sh app      # 仅构建 yudao-ui-deepay-app (uni-app H5)
#    bash build.sh all      # 同不带参数
#
#  前置条件：Node 18 ~ 24（不含 25，推荐 20 LTS），npm ≥ 9
#    宝塔面板 → 软件商店 → Node.js 20 LTS
# =============================================================
set -euo pipefail

# 脚本自身所在目录（无论从哪里执行都正确）
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET="${1:-all}"

# ── 彩色输出 ─────────────────────────────────────────────────
G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; N='\033[0m'
info() { echo -e "${G}[INFO]${N} $*"; }
step() { echo -e "\n${C}===== $* =====${N}"; }
ok()   { echo -e "${G}  ✓  $*${N}"; }
err()  { echo -e "${R}[ERR] $*${N}"; exit 1; }

# ── 安装 + 构建辅助函数 ───────────────────────────────────────
_install() {
  if [ -f package-lock.json ]; then
    info "发现 package-lock.json → npm ci"
    npm ci
  else
    info "未发现 lockfile → npm install"
    npm install
  fi
}

# ── 构建 yudao-ui-deepay（Vue3 + Vite PWA）────────────────────
build_admin() {
  step "构建 yudao-ui-deepay  (Vue3 PWA)"
  cd "$ROOT/yudao-ui-deepay"
  info "目录: $(pwd)"
  _install
  npm run build
  ok "产物 → $ROOT/yudao-ui-deepay/dist/"
}

# ── 构建 yudao-ui-deepay-app（uni-app H5）────────────────────
build_app() {
  step "构建 yudao-ui-deepay-app  (uni-app H5)"
  cd "$ROOT/yudao-ui-deepay-app"
  info "目录: $(pwd)"
  _install
  npm run build:h5
  ok "产物 → $ROOT/yudao-ui-deepay-app/dist/build/h5/"
}

# ── 主流程 ────────────────────────────────────────────────────
info "Node : $(node -v 2>/dev/null || echo '未找到')"
info "npm  : $(npm  -v 2>/dev/null || echo '未找到')"
command -v node >/dev/null 2>&1 || err "未找到 node，请先安装 Node.js 18~20 LTS"

case "$TARGET" in
  admin) build_admin ;;
  app)   build_app   ;;
  all)   build_admin && build_app ;;
  *) err "未知参数: $TARGET  →  用法: bash build.sh [admin|app|all]" ;;
esac

echo ""
ok "全部构建完成 🎉"
echo ""
echo "  产物目录："
echo "    PWA  → $ROOT/yudao-ui-deepay/dist/"
echo "    H5   → $ROOT/yudao-ui-deepay-app/dist/build/h5/"
echo ""
echo "  部署提示："
echo "    PWA 产物 → 复制到 Nginx 静态根目录（/www/wwwroot/deepay.srl/）"
echo "    H5  产物 → 复制到 Nginx H5 子目录（如 /www/wwwroot/deepay.srl/app/）"
