#!/bin/bash
# 初始化瀚高测试库：使用 PostgreSQL 客户端 psql 创建全新的数据库并导入 sql/highgo 脚本，数据库已存在（包括空库）时拒绝导入；任一步骤失败都以非零状态退出
# 只支持 PostgreSQL 客户端可用的认证方式（例如 md5、scram-sha-256）；数据库开启 SM3 等瀚高专有认证时，请使用瀚高客户端手动导入
set -euo pipefail

host="${HIGHGO_HOST:-highgo}"
port="${HIGHGO_PORT:-5866}"
user="${HIGHGO_USER:-highgo}"
maintenance_db="${HIGHGO_MAINTENANCE_DB:-highgo}"
database="${HIGHGO_DATABASE:-ruoyi-vue-pro}"
export PGPASSWORD="${HIGHGO_PASSWORD:?HIGHGO_PASSWORD 未设置，请设置为瀚高数据库账号的实际密码}"
export PGCONNECT_TIMEOUT=5
export PGOPTIONS="-c client_min_messages=warning"

if [[ ! "${database}" =~ ^[A-Za-z0-9_-]+$ ]]; then
    echo "数据库名 ${database} 不合法，只允许字母、数字、下划线和中划线" >&2
    exit 1
fi

client=(psql -h "${host}" -p "${port}" -U "${user}" -X -q -v ON_ERROR_STOP=1)

# 1. 等待瀚高可连接
for i in $(seq 1 60); do
    if pg_isready -h "${host}" -p "${port}" -U "${user}" -d "${maintenance_db}" -q; then
        break
    fi
    if [ "${i}" -eq 60 ]; then
        echo "等待瀚高 ${host}:${port} 超时，请执行 docker compose logs highgo 检查启动日志" >&2
        exit 1
    fi
    sleep 5
done

# 2. 校验账号、密码及认证方式
if ! "${client[@]}" -d "${maintenance_db}" -c 'SELECT 1' >/dev/null; then
    echo "无法使用账号 ${user} 登录 ${maintenance_db} 数据库：请检查 HIGHGO_USER、HIGHGO_PASSWORD、HIGHGO_MAINTENANCE_DB；如果数据库使用 SM3 等专有认证，请使用瀚高客户端手动导入" >&2
    exit 1
fi

# 3. 创建全新数据库：不判断是否存在，数据库已存在时直接失败，避免项目 SQL 中的 DROP TABLE 重建已有的同名表
if ! "${client[@]}" -d "${maintenance_db}" -c "CREATE DATABASE \"${database}\";"; then
    echo "创建数据库 ${database} 失败：只允许初始化全新的数据库，数据库已存在（包括空库）时拒绝导入；如需重新初始化，请删除瀚高数据卷后重建" >&2
    exit 1
fi

# 4. 按 quartz、ruoyi-vue-pro 的顺序导入脚本（ON_ERROR_STOP 遇到错误立即退出，失败时保留数据库，便于排查）
"${client[@]}" -d "${database}" -f /tmp/quartz.sql
"${client[@]}" -d "${database}" -f /tmp/schema.sql

# 注意：瀚高的 information_schema.tables 对同名表可能返回多行，这里直接查 pg_class（每张表一行），只统计普通表、分区表
table_count=$("${client[@]}" -d "${database}" -At -c "SELECT COUNT(*) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = current_schema() AND c.relkind IN ('r', 'p');")
echo "瀚高初始化完成：数据库 ${database} 共 ${table_count} 张表"
