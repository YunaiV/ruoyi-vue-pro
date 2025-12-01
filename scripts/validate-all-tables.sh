#!/bin/bash
# 数据库表结构全面验证脚本
# 验证所有模块的表是否存在以及核心字段

set -e

# 数据库连接配置
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-ruoyi-vue-pro}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-Pass1234}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 执行 PostgreSQL 查询
pg_query() {
    PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "$1" 2>/dev/null
}

# 验证表是否存在
check_table_exists() {
    local table_name="$1"
    local count=$(pg_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='$table_name';")
    if [ "$count" -eq 1 ]; then
        return 0
    else
        return 1
    fi
}

# 获取表记录数
get_table_count() {
    local table_name="$1"
    pg_query "SELECT COUNT(*) FROM $table_name WHERE deleted = false;" 2>/dev/null || echo "0"
}

# 定义各模块的核心表
declare -A MODULE_TABLES
MODULE_TABLES[system]="system_users system_role system_menu system_dept system_post system_dict_type system_dict_data system_tenant system_oauth2_client system_notice system_login_log system_operate_log"
MODULE_TABLES[infra]="infra_config infra_file_config infra_job infra_codegen_table infra_api_access_log infra_api_error_log"
MODULE_TABLES[bpm]="bpm_category bpm_form bpm_process_definition_info bpm_user_group bpm_oa_leave bpm_process_instance_copy"
MODULE_TABLES[pay]="pay_app pay_channel pay_order pay_refund pay_notify_task pay_notify_log pay_wallet pay_wallet_transaction"
MODULE_TABLES[member]="member_user member_level member_tag member_point_record member_sign_in_record"
MODULE_TABLES[crm]="crm_customer crm_contract crm_clue crm_contact crm_business crm_receivable"
MODULE_TABLES[mall_product]="product_spu product_sku product_category product_brand product_property"
MODULE_TABLES[mall_trade]="trade_order trade_order_item trade_after_sale trade_cart"
MODULE_TABLES[mall_promotion]="promotion_coupon promotion_coupon_template promotion_seckill_activity promotion_combination_activity"
MODULE_TABLES[ai]="ai_chat_role ai_chat_conversation ai_chat_message ai_knowledge ai_api_key ai_model"
MODULE_TABLES[iot]="iot_product iot_device iot_thing_model"
MODULE_TABLES[mp]="mp_account mp_user mp_tag mp_menu mp_message mp_auto_reply"
MODULE_TABLES[report]="report_go_view_project"

echo ""
echo "=========================================="
echo "   数据库表结构全面验证工具"
echo "=========================================="
echo ""

# 测试数据库连接
log_info "测试数据库连接..."
if ! pg_query "SELECT 1;" > /dev/null 2>&1; then
    log_error "无法连接到数据库"
    exit 1
fi
log_success "数据库连接成功 ($DB_HOST:$DB_PORT/$DB_NAME)"
echo ""

# 统计变量
total_tables=0
found_tables=0
missing_tables=0

# 遍历各模块验证
for module in "${!MODULE_TABLES[@]}"; do
    echo "----------------------------------------"
    log_info "验证模块: $module"

    tables="${MODULE_TABLES[$module]}"
    module_found=0
    module_missing=0

    for table in $tables; do
        total_tables=$((total_tables + 1))
        if check_table_exists "$table"; then
            count=$(get_table_count "$table")
            log_success "  $table (记录数: $count)"
            found_tables=$((found_tables + 1))
            module_found=$((module_found + 1))
        else
            log_error "  $table [不存在]"
            missing_tables=$((missing_tables + 1))
            module_missing=$((module_missing + 1))
        fi
    done

    if [ $module_missing -eq 0 ]; then
        echo -e "  ${GREEN}模块验证通过 ✓${NC}"
    else
        echo -e "  ${RED}模块有 $module_missing 个表缺失 ✗${NC}"
    fi
done

echo ""
echo "=========================================="
echo "   验证结果汇总"
echo "=========================================="
echo -e "  总表数: $total_tables"
echo -e "  找到: ${GREEN}$found_tables${NC}"
echo -e "  缺失: ${RED}$missing_tables${NC}"
echo ""

if [ $missing_tables -eq 0 ]; then
    log_success "所有表结构验证通过！"
    exit 0
else
    log_error "有 $missing_tables 个表缺失，请检查数据库初始化脚本"
    exit 1
fi
