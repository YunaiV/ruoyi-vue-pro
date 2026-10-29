#!/bin/bash
# 初始化 GaussDB 数据库：使用 GaussDB 的 gsql 客户端，创建 PG 兼容模式（DBCOMPATIBILITY = 'PG'）、UTF8 编码的全新数据库，并导入 sql/opengauss 脚本
# GaussDB 已内置 dual 视图（sys.dual、pg_catalog.dual），导入时只跳过 sql/opengauss/ruoyi-vue-pro.sql 开头创建 dual 表的一段，不修改内置视图，其余 SQL 保持不变
# 数据库已存在（包括空库）时拒绝导入；任一步骤失败都以非零状态退出
# 执行：GAUSSDB_PORT=8000 GAUSSDB_USER='数据库账号' GAUSSDB_PASSWORD='数据库密码' bash sql/tools/gaussdb/create_schema.sh
set -euo pipefail

host="${GAUSSDB_HOST:-}" # 为空时，使用本机的 Unix Socket 连接
port="${GAUSSDB_PORT:?GAUSSDB_PORT 未设置，请设置为 GaussDB 的端口}"
user="${GAUSSDB_USER:?GAUSSDB_USER 未设置，请设置为具有 CREATEDB 权限的数据库账号}"
password="${GAUSSDB_PASSWORD:?GAUSSDB_PASSWORD 未设置，请设置为数据库账号的实际密码}"
maintenance_db="${GAUSSDB_MAINTENANCE_DB:-postgres}"
database="${GAUSSDB_DATABASE:-ruoyi-vue-pro}"
sql_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../opengauss" && pwd)"

if [[ ! "${database}" =~ ^[A-Za-z0-9_-]+$ ]]; then
    echo "数据库名 ${database} 不合法，只允许字母、数字、下划线和中划线" >&2
    exit 1
fi
if ! command -v gsql >/dev/null 2>&1; then
    echo "未找到 gsql 客户端：请在安装了 GaussDB gsql 客户端的机器上执行，例如以数据库安装用户登录 GaussDB 服务器" >&2
    exit 1
fi

client=(gsql -p "${port}" -U "${user}" -W "${password}" -X -q -v ON_ERROR_STOP=1)
if [ -n "${host}" ]; then
    client+=(-h "${host}")
fi

# 1. 生成跳过 dual 段的临时脚本：只允许跳过「-- Table structure for dual」到其后第一个「-- @formatter:on」之间的 dual 建表、注释及插入语句
schema_file="$(mktemp)"
trap 'rm -f "${schema_file}"' EXIT
# 注释语句只允许单个字符串常量（内部的单引号写作 ''），字符串后只能是行尾的分号，避免同一行中的其它 SQL 被一起跳过
if ! awk -v q="'" '
    BEGIN { comment = "^COMMENT ON TABLE dual IS " q "([^" q "]|" q q ")*" q ";$" }
    /^-- Table structure for dual$/ { skipping = 1; blocks++ }
    skipping {
        if ($0 ~ /^(DROP TABLE IF EXISTS dual;|CREATE TABLE dual|INSERT INTO dual VALUES \(1\);)$/) {
            statements++
        } else if ($0 !~ /^(--.*|\(|[[:space:]]*id int2|\);)?$/ && $0 !~ comment) {
            unexpected = 1
        }
        if ($0 ~ /^-- @formatter:on$/) {
            skipping = 0
        }
        next
    }
    { print }
    END { exit (blocks == 1 && statements == 3 && !skipping && !unexpected) ? 0 : 1 }
' "${sql_dir}/ruoyi-vue-pro.sql" > "${schema_file}" \
    || grep -Eq '^(DROP TABLE IF EXISTS dual;|CREATE TABLE dual|INSERT INTO dual )' "${schema_file}"; then
    echo "${sql_dir}/ruoyi-vue-pro.sql 中 dual 表一段的格式与预期不一致，拒绝导入；请检查脚本是否由 sql/tools/convertor.py 生成" >&2
    exit 1
fi

# 2. 校验账号、密码及认证方式
if ! "${client[@]}" -d "${maintenance_db}" -c 'SELECT 1' >/dev/null; then
    echo "无法使用账号 ${user} 登录 ${maintenance_db} 数据库：请检查 GAUSSDB_HOST、GAUSSDB_PORT、GAUSSDB_USER、GAUSSDB_PASSWORD，以及 gs_hba.conf 中该账号、客户端地址的认证配置" >&2
    exit 1
fi

# 3. 校验 GaussDB 内置的 dual 视图：openGauss、MogDB 没有该视图，请直接导入 sql/opengauss 脚本
dual_count=$("${client[@]}" -d "${maintenance_db}" -At -c "SELECT COUNT(*) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'dual' AND c.relkind = 'v' AND n.nspname IN ('sys', 'pg_catalog');")
if [ "${dual_count}" = "0" ]; then
    echo "未检测到 GaussDB 内置的 dual 视图，拒绝导入：该脚本只适用于 GaussDB，openGauss、MogDB 请直接导入 sql/opengauss 脚本" >&2
    exit 1
fi

# 4. 创建全新数据库：不判断是否存在，数据库已存在时直接失败，避免项目 SQL 中的 DROP TABLE 重建已有的同名表
# 注意：GaussDB 默认创建的是 A（Oracle）兼容模式的数据库，项目脚本需要使用 PG 兼容模式
if ! "${client[@]}" -d "${maintenance_db}" -c "CREATE DATABASE \"${database}\" ENCODING 'UTF8' DBCOMPATIBILITY = 'PG';"; then
    echo "创建数据库 ${database} 失败：只允许初始化全新的数据库，数据库已存在（包括空库）时拒绝导入；如需重新初始化，请确认数据可以删除后，手动删除该数据库" >&2
    exit 1
fi
compatibility=$("${client[@]}" -d "${database}" -At -c 'SHOW sql_compatibility;')
if [ "${compatibility}" != "PG" ]; then
    echo "数据库 ${database} 的兼容模式为 ${compatibility}，不是 PG，拒绝导入" >&2
    exit 1
fi

# 5. 按 quartz、ruoyi-vue-pro 的顺序导入脚本（ON_ERROR_STOP 遇到错误立即退出，失败时保留数据库，便于排查）
"${client[@]}" -d "${database}" -f "${sql_dir}/quartz.sql"
"${client[@]}" -d "${database}" -f "${schema_file}"

# 只统计普通表、分区表；dual 使用 GaussDB 内置视图，不计入
table_count=$("${client[@]}" -d "${database}" -At -c "SELECT COUNT(*) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = current_schema() AND c.relkind IN ('r', 'p');")
echo "GaussDB 初始化完成：数据库 ${database} 共 ${table_count} 张表"
