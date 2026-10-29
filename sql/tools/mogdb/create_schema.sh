#!/bin/bash
# 初始化 MogDB 测试库：在 mogdb 容器内以 omm 用户执行，创建 PG 兼容模式（DBCOMPATIBILITY = 'PG'）、UTF8 编码的全新数据库，并导入 sql/opengauss 脚本
# 数据库已存在（包括空库）时拒绝导入；任一步骤失败都以非零状态退出
# 执行：docker compose exec -u omm mogdb bash /tmp/create_schema.sh
set -euo pipefail

# 官方镜像只在 omm 用户的 .profile 中设置 gsql 的 PATH，这里显式设置
export GAUSSHOME="${GAUSSHOME:-/usr/local/mogdb}"
export PATH="${GAUSSHOME}/bin:${PATH}"
export LD_LIBRARY_PATH="${GAUSSHOME}/lib:${LD_LIBRARY_PATH:-}"

port="${GS_PORT:-5432}"
owner="${MOGDB_OWNER:-mogdb}" # 应用连接使用的账号，官方镜像默认创建 mogdb 用户；omm 初始用户不允许远程连接
password="${GS_PASSWORD:?GS_PASSWORD 未设置，请检查 docker-compose.yaml 中 mogdb 服务的环境变量}"
database="${MOGDB_DATABASE:-ruoyi-vue-pro}"

if [[ ! "${database}" =~ ^[A-Za-z0-9_-]+$ ]]; then
    echo "数据库名 ${database} 不合法，只允许字母、数字、下划线和中划线" >&2
    exit 1
fi
if [[ ! "${owner}" =~ ^[A-Za-z0-9_]+$ ]]; then
    echo "用户名 ${owner} 不合法，只允许字母、数字和下划线" >&2
    exit 1
fi
if [ "$(id -un)" != "omm" ]; then
    echo "请使用 omm 用户执行：docker compose exec -u omm mogdb bash /tmp/create_schema.sh" >&2
    exit 1
fi

client=(gsql -p "${port}" -X -q -v ON_ERROR_STOP=1)

# 1. 等待 MogDB 可连接（首次启动时需要初始化数据目录）
for i in $(seq 1 60); do
    if "${client[@]}" -d postgres -c 'SELECT 1' >/dev/null 2>&1; then
        break
    fi
    if [ "${i}" -eq 60 ]; then
        echo "等待 MogDB 启动超时，请执行 docker compose logs mogdb 检查启动日志" >&2
        exit 1
    fi
    sleep 5
done

# 2. 校验数据库所有者
if [ "$("${client[@]}" -d postgres -At -c "SELECT COUNT(*) FROM pg_catalog.pg_roles WHERE rolname = '${owner}';")" != "1" ]; then
    echo "用户 ${owner} 不存在：请通过 MOGDB_OWNER 指定应用连接使用的账号" >&2
    exit 1
fi

# 3. 创建全新数据库：不判断是否存在，数据库已存在时直接失败，避免项目 SQL 中的 DROP TABLE 重建已有的同名表
# 注意：MogDB 默认创建的是 A（Oracle）兼容模式的数据库，导入项目脚本会失败，需要使用 PG 兼容模式
if ! "${client[@]}" -d postgres -c "CREATE DATABASE \"${database}\" OWNER \"${owner}\" ENCODING 'UTF8' DBCOMPATIBILITY = 'PG';"; then
    echo "创建数据库 ${database} 失败：只允许初始化全新的数据库，数据库已存在（包括空库）时拒绝导入；如需重新初始化，请删除 MogDB 数据卷后重建" >&2
    exit 1
fi

# 4. 使用数据库所有者，按 quartz、ruoyi-vue-pro 的顺序导入脚本（ON_ERROR_STOP 遇到错误立即退出，失败时保留数据库，便于排查）
owner_client=("${client[@]}" -U "${owner}" -W "${password}" -d "${database}")
"${owner_client[@]}" -f /tmp/quartz.sql
"${owner_client[@]}" -f /tmp/schema.sql

# 只统计普通表、分区表
table_count=$("${owner_client[@]}" -At -c "SELECT COUNT(*) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = current_schema() AND c.relkind IN ('r', 'p');")
echo "MogDB 初始化完成：数据库 ${database} 共 ${table_count} 张表"
