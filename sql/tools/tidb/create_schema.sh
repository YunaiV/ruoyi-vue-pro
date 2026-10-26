#!/bin/bash
# 初始化 TiDB 测试库：只允许创建全新的数据库并导入项目 SQL，数据库已存在（包括空库）时拒绝导入；任一步骤失败都以非零状态退出
set -euo pipefail

host="${TIDB_HOST:-tidb}"
port="${TIDB_PORT:-4000}"
database="${TIDB_DATABASE:-ruoyi-vue-pro}"
password="${TIDB_PASSWORD:?TIDB_PASSWORD 未设置}"

if [[ ! "${database}" =~ ^[A-Za-z0-9_-]+$ ]]; then
    echo "数据库名 ${database} 不合法，只允许字母、数字、下划线和中划线" >&2
    exit 1
fi
# 密码会拼接到 ALTER USER 语句中，只允许不需要转义的字符
if [[ ! "${password}" =~ ^[A-Za-z0-9_.@#%+=-]+$ ]]; then
    echo "TIDB_PASSWORD 不合法，只允许字母、数字和 _ . @ # % + = - 字符" >&2
    exit 1
fi

client=(mysql -h"${host}" -P"${port}" -uroot --default-character-set=utf8mb4)

# 1. 等待 TiDB 可连接（首次启动时 PD、TiKV 需要一段时间完成初始化）
for i in $(seq 1 60); do
    if mysqladmin -h"${host}" -P"${port}" -uroot --connect-timeout=3 ping >/dev/null 2>&1; then
        break
    fi
    if [ "${i}" -eq 60 ]; then
        echo "等待 TiDB ${host}:${port} 超时，请执行 docker compose logs tidb 检查启动日志" >&2
        exit 1
    fi
    sleep 5
done

# 2. 新集群的 root 默认没有密码，首次初始化时设置为 TIDB_PASSWORD
export MYSQL_PWD="${password}"
if ! "${client[@]}" -e 'SELECT 1' >/dev/null 2>&1; then
    env -u MYSQL_PWD "${client[@]}" -e "ALTER USER 'root'@'%' IDENTIFIED BY '${password}';"
fi

# 3. 创建全新数据库：不使用 IF NOT EXISTS，数据库已存在时直接失败，避免项目 SQL 中的 DROP TABLE 重建已有的同名表
if ! "${client[@]}" -e "CREATE DATABASE \`${database}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"; then
    echo "创建数据库 ${database} 失败：只允许初始化全新的数据库，数据库已存在（包括空库）时拒绝导入；如需重新初始化，请删除 TiDB 数据卷后重建" >&2
    exit 1
fi

# 4. 按 ruoyi-vue-pro、quartz 的顺序导入 MySQL 脚本（mysql 客户端遇到错误会立即退出，失败时保留数据库，便于排查）
"${client[@]}" -D "${database}" < /tmp/schema.sql
"${client[@]}" -D "${database}" < /tmp/quartz.sql

table_count=$("${client[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = '${database}';")
echo "TiDB 初始化完成：数据库 ${database} 共 ${table_count} 张表"
