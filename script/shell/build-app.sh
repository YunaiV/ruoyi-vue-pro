#!/usr/bin/env bash
# ===========================================================================
# build-app.sh — 一键安装依赖并构建 yudao-ui-deepay-app（uni-app / H5）
#
# 用法：
#   bash script/shell/build-app.sh          # 默认构建 H5
#   bash script/shell/build-app.sh h5       # 构建 H5
#   bash script/shell/build-app.sh mp-weixin  # 构建微信小程序
#   bash script/shell/build-app.sh app      # 构建原生 App
#
# 前置条件：
#   - Node 18 ~ 24（不含 25，推荐 Node 20 LTS），npm ≥ 9
#   - 如已安装 nvm：nvm install 20 && nvm use 20
# ===========================================================================
set -euo pipefail

TARGET="${1:-h5}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(cd "$SCRIPT_DIR/../../yudao-ui-deepay-app" && pwd)"

echo "===> [build-app] target=$TARGET  dir=$APP_DIR"
cd "$APP_DIR"

# ── 安装依赖 ──────────────────────────────────────────────────────────────
if [ -f package-lock.json ]; then
  echo "===> 发现 package-lock.json，使用 npm ci（可重复安装）"
  npm ci
else
  echo "===> 未发现 lockfile，使用 npm install"
  npm install
fi

# ── 构建 ──────────────────────────────────────────────────────────────────
echo "===> 执行 npm run build:$TARGET"
npm run "build:$TARGET"

echo "===> 构建完成！产物目录：$APP_DIR/dist/build/$TARGET"
