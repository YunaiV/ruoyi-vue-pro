#!/bin/bash
# 数据库结构验证脚本
# 用于验证 PostgreSQL 数据库表结构与 Java 实体类的一致性
#
# 使用方法: ./scripts/db-schema-validator.sh [模块名]
# 示例: ./scripts/db-schema-validator.sh ai
#       ./scripts/db-schema-validator.sh system
#       ./scripts/db-schema-validator.sh         # 验证所有模块

set -e

# 数据库连接配置
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-ruoyi-vue-pro}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-Pass1234}"

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_detail() { echo -e "${CYAN}      ${NC} $1"; }

# 执行 PostgreSQL 查询
pg_query() {
    PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "$1" 2>/dev/null
}

# 获取数据库中所有表
get_db_tables() {
    local prefix="$1"
    if [ -n "$prefix" ]; then
        pg_query "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE '${prefix}_%' ORDER BY table_name;"
    else
        pg_query "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;"
    fi
}

# 获取表的列名列表
get_table_column_names() {
    local table_name="$1"
    pg_query "SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '$table_name' ORDER BY ordinal_position;"
}

# 获取列的数据类型
get_column_type() {
    local table_name="$1"
    local column_name="$2"
    pg_query "SELECT data_type FROM information_schema.columns WHERE table_schema='public' AND table_name='$table_name' AND column_name='$column_name';"
}

# 将表名转换为类名（去掉前缀，转为驼峰）
table_to_class_name() {
    local table_name="$1"
    # 去掉模块前缀 (如 system_, ai_, bpm_ 等)
    local base_name=$(echo "$table_name" | sed 's/^[a-z]*_//')
    # 转换为驼峰命名
    echo "$base_name" | sed -r 's/(^|_)([a-z])/\U\2/g'
}

# 在项目中查找 DO 类文件
find_do_class() {
    local class_name="$1"
    local module_path="$2"

    # 尝试多种命名模式
    local patterns=(
        "${class_name}DO.java"
        "${class_name}.java"
    )

    for pattern in "${patterns[@]}"; do
        local found=$(find "$module_path" -type f -name "$pattern" -path "*/dataobject/*" 2>/dev/null | grep -v target | head -1)
        if [ -n "$found" ]; then
            echo "$found"
            return 0
        fi
    done

    # 如果在 dataobject 目录没找到，尝试全局搜索
    for pattern in "${patterns[@]}"; do
        local found=$(find "$module_path" -type f -name "$pattern" 2>/dev/null | grep -v target | head -1)
        if [ -n "$found" ]; then
            echo "$found"
            return 0
        fi
    done

    return 1
}

# 验证单个模块
validate_module() {
    local module_prefix="$1"
    local module_path="$2"
    local errors=0
    local warnings=0
    local tables_checked=0

    log_info "验证模块: $module_prefix"
    log_detail "路径: $module_path"
    echo "=================================================="

    # 获取该模块的所有数据库表
    local tables=$(get_db_tables "$module_prefix")

    if [ -z "$tables" ]; then
        log_warn "未找到前缀为 '${module_prefix}_' 的表"
        return 0
    fi

    local table_count=$(echo "$tables" | wc -l)
    log_info "找到 $table_count 张表"

    for table in $tables; do
        ((tables_checked++))
        echo ""
        log_info "[$tables_checked/$table_count] 检查表: $table"

        # 获取数据库中的列
        local db_columns=$(get_table_column_names "$table")

        # 查找对应的 DO 类
        local class_name=$(table_to_class_name "$table")
        local do_file=$(find_do_class "$class_name" "$module_path")

        if [ -z "$do_file" ]; then
            log_warn "  未找到对应的 Java 实体类: ${class_name}DO.java"
            ((warnings++))

            # 仍然检查基础字段
            log_detail "  继续检查数据库字段..."
        else
            log_success "  实体类: $(basename $do_file)"
        fi

        # 检查基础审计字段
        local required_columns="id creator create_time updater update_time deleted"
        local missing_fields=0
        for col in $required_columns; do
            if ! echo "$db_columns" | grep -q "^${col}$"; then
                log_error "  缺少必需字段: $col"
                ((errors++))
                ((missing_fields++))
            fi
        done

        if [ $missing_fields -eq 0 ]; then
            log_success "  基础审计字段完整 ✓"
        fi

        # 检查 deleted 字段类型
        local deleted_type=$(get_column_type "$table" "deleted")
        if [ -n "$deleted_type" ]; then
            if [ "$deleted_type" != "boolean" ]; then
                log_error "  字段 'deleted' 类型错误: 期望 boolean, 实际 $deleted_type"
                ((errors++))
            else
                log_success "  字段 'deleted' 类型: boolean ✓"
            fi
        fi

        # 检查是否需要多租户字段
        if [ -n "$do_file" ]; then
            if grep -q "TenantBaseDO" "$do_file" 2>/dev/null; then
                if ! echo "$db_columns" | grep -q "^tenant_id$"; then
                    log_error "  实体类继承 TenantBaseDO 但表缺少 tenant_id 字段"
                    ((errors++))
                else
                    log_success "  多租户字段 tenant_id 存在 ✓"
                fi
            else
                # BaseDO 不需要 tenant_id
                if echo "$db_columns" | grep -q "^tenant_id$"; then
                    log_warn "  实体类未继承 TenantBaseDO 但表有 tenant_id 字段 (可能需要检查)"
                fi
            fi
        fi

    done

    echo ""
    echo "=================================================="
    echo -e "${BLUE}模块 $module_prefix 验证结果:${NC}"
    echo -e "  检查表数: $tables_checked"
    echo -e "  错误数: ${RED}$errors${NC}"
    echo -e "  警告数: ${YELLOW}$warnings${NC}"

    return $errors
}

# 快速验证所有表的 deleted 字段
quick_validate_deleted_field() {
    log_info "快速验证所有表的 deleted 字段类型..."
    echo "=================================================="

    local errors=0

    # 获取统计信息
    local total=$(pg_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND column_name = 'deleted';")
    local correct_type=$(pg_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND column_name = 'deleted' AND data_type = 'boolean';")
    local wrong_type=$((total - correct_type))

    if [ "$total" -eq 0 ]; then
        log_warn "没有找到包含 deleted 字段的表"
        return 0
    fi

    # 如果有错误类型，列出具体的表
    if [ "$wrong_type" -gt 0 ]; then
        log_error "发现 $wrong_type 张表的 deleted 字段类型不是 boolean:"
        pg_query "SELECT table_name || ': ' || data_type FROM information_schema.columns WHERE table_schema = 'public' AND column_name = 'deleted' AND data_type != 'boolean' ORDER BY table_name;" | while read line; do
            log_error "  $line"
        done
        errors=$wrong_type
    fi

    echo ""
    log_info "deleted 字段检查结果:"
    echo -e "  总表数: $total"
    echo -e "  类型正确 (boolean): ${GREEN}$correct_type${NC}"
    echo -e "  类型错误: ${RED}$wrong_type${NC}"

    if [ "$wrong_type" -eq 0 ]; then
        log_success "所有表的 deleted 字段类型正确！"
    fi

    return $errors
}

# 主验证函数
main() {
    local target_module="$1"
    local total_errors=0

    echo ""
    echo "=========================================="
    echo "   数据库结构验证工具 - PostgreSQL"
    echo "=========================================="
    echo ""

    # 测试数据库连接
    log_info "测试数据库连接..."
    log_detail "主机: $DB_HOST:$DB_PORT"
    log_detail "数据库: $DB_NAME"

    if ! pg_query "SELECT 1;" > /dev/null 2>&1; then
        log_error "无法连接到数据库"
        exit 1
    fi
    log_success "数据库连接成功"
    echo ""

    # 定义模块映射
    declare -A modules=(
        ["system"]="yudao-module-system"
        ["infra"]="yudao-module-infra"
        ["bpm"]="yudao-module-bpm"
        ["ai"]="yudao-module-ai"
        ["pay"]="yudao-module-pay"
        ["member"]="yudao-module-member"
        ["crm"]="yudao-module-crm"
        ["erp"]="yudao-module-erp"
        ["mp"]="yudao-module-mp"
    )

    if [ "$target_module" == "--quick" ] || [ "$target_module" == "-q" ]; then
        # 快速验证模式
        quick_validate_deleted_field
        total_errors=$?
    elif [ -n "$target_module" ]; then
        # 验证指定模块
        local module_path="${modules[$target_module]}"
        if [ -z "$module_path" ]; then
            log_error "未知模块: $target_module"
            log_info "可用模块: ${!modules[*]}"
            log_info "或使用 --quick/-q 进行快速验证"
            exit 1
        fi
        validate_module "$target_module" "$PROJECT_ROOT/$module_path"
        total_errors=$?
    else
        # 先做快速验证
        quick_validate_deleted_field
        total_errors=$?
        echo ""

        # 然后验证所有模块
        for prefix in "${!modules[@]}"; do
            echo ""
            validate_module "$prefix" "$PROJECT_ROOT/${modules[$prefix]}"
            total_errors=$((total_errors + $?))
        done
    fi

    echo ""
    echo "=========================================="
    if [ $total_errors -eq 0 ]; then
        log_success "所有验证通过！"
    else
        log_error "验证完成，共发现 $total_errors 个错误"
    fi
    echo "=========================================="

    exit $total_errors
}

# 运行主函数
main "$@"
