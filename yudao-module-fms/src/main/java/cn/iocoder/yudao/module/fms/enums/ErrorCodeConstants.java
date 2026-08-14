package cn.iocoder.yudao.module.fms.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * FMS 错误码常量
 *
 * @author 芋道源码
 */
public interface ErrorCodeConstants {

    // ========== 账套 1-052-100-000 ==========
    ErrorCode ACCOUNT_SET_NOT_EXISTS = new ErrorCode(1_052_100_000, "账套不存在");
    ErrorCode ACCOUNT_SET_CODE_DUPLICATE = new ErrorCode(1_052_100_001, "账套编码已存在");
    ErrorCode ACCOUNT_SET_ACCESS_DENIED = new ErrorCode(1_052_100_002, "无权访问该账套");
    ErrorCode ACCOUNT_SET_ALREADY_INITIALIZED = new ErrorCode(1_052_100_003, "账套已完成初始化");
    ErrorCode ACCOUNT_SET_NOT_INITIALIZED = new ErrorCode(1_052_100_005, "账套尚未完成初始化");
    ErrorCode ACCOUNT_SET_SETTINGS_RULE_INVALID = new ErrorCode(1_052_100_006, "科目编码规则不正确");
    ErrorCode ACCOUNT_SET_SETTINGS_RULE_SHRINK = new ErrorCode(1_052_100_007, "科目级次和编码长度不能调小");
    ErrorCode FINANCE_PARAMETER_NOT_EXISTS = new ErrorCode(1_052_100_008, "财务参数不存在");

    // ========== 科目 1-052-101-000 ==========
    ErrorCode SUBJECT_NOT_EXISTS = new ErrorCode(1_052_101_000, "科目不存在");
    ErrorCode SUBJECT_CODE_DUPLICATE = new ErrorCode(1_052_101_001, "科目编码已存在");
    ErrorCode SUBJECT_CATEGORY_INVALID = new ErrorCode(1_052_101_002, "科目类别与科目类型不匹配");
    ErrorCode SUBJECT_PARENT_INVALID = new ErrorCode(1_052_101_003,
            "上级科目不属于当前账套，或科目类型、类别不一致");
    ErrorCode SUBJECT_CODE_RULE_INVALID = new ErrorCode(1_052_101_004, "科目编码不符合科目级次规则");
    ErrorCode SUBJECT_HAS_CHILDREN = new ErrorCode(1_052_101_005, "科目下有子级科目，不能删除或修改编码");
    ErrorCode SUBJECT_QUANTITY_UNIT_REQUIRED = new ErrorCode(1_052_101_006, "启用数量核算时数量单位不能为空");
    ErrorCode SUBJECT_CURRENCY_INVALID = new ErrorCode(1_052_101_007, "外币核算不能选择本位币");
    ErrorCode SUBJECT_VOUCHER_ENTRY_IN_USE = new ErrorCode(1_052_101_008,
            "科目已被 {} 条凭证分录使用，不能删除");
    ErrorCode SUBJECT_VOUCHER_TEMPLATE_IN_USE = new ErrorCode(1_052_101_009,
            "科目已被 {} 个凭证模板使用，不能删除");
    ErrorCode SUBJECT_INITIAL_BALANCE_IN_USE = new ErrorCode(1_052_101_010,
            "科目已被 {} 条初始余额使用，不能删除");
    ErrorCode SUBJECT_AUXILIARY_COMBINATION_IN_USE = new ErrorCode(1_052_101_011,
            "科目已被 {} 个辅助核算组合使用，不能删除");
    ErrorCode SUBJECT_CLOSING_SCHEME_IN_USE = new ErrorCode(1_052_101_012,
            "科目已被 {} 个结账方案使用，不能删除");
    ErrorCode SUBJECT_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_052_101_013, "导入科目数据不能为空");
    ErrorCode SUBJECT_USED_BALANCE_DIRECTION_IMMUTABLE = new ErrorCode(1_052_101_014,
            "科目已有业务数据，不能修改余额方向");
    ErrorCode SUBJECT_USED_AUXILIARY_IMMUTABLE = new ErrorCode(1_052_101_015,
            "科目已有业务数据，不能直接修改辅助核算");
    ErrorCode SUBJECT_NON_LEAF_CONFIG_IMMUTABLE = new ErrorCode(1_052_101_016,
            "非末级科目不能修改科目类型、类别或辅助核算");
    ErrorCode SUBJECT_QUANTITY_ACCOUNTING_IN_USE = new ErrorCode(1_052_101_017,
            "科目已有数量数据，不能关闭数量核算");
    ErrorCode SUBJECT_PARENT_IN_USE = new ErrorCode(1_052_101_018,
            "上级科目已有业务数据，新增下级前需要迁移历史数据");
    ErrorCode SUBJECT_PARENT_CONFIG_INCOMPATIBLE = new ErrorCode(1_052_101_019,
            "迁移历史数据时，下级科目的余额方向和核算配置必须与上级科目一致");
    ErrorCode SUBJECT_AUXILIARY_MIGRATION_INVALID = new ErrorCode(1_052_101_020,
            "科目已有业务数据，新增辅助核算时必须为每个类别选择有效的迁移项目");
    ErrorCode SUBJECT_CLOSING_TEMPLATE_IN_USE = new ErrorCode(1_052_101_021,
            "科目已被 {} 个结账模板使用，不能删除");

    // ========== 辅助核算 1-052-102-000 ==========
    ErrorCode AUXILIARY_TYPE_NOT_EXISTS = new ErrorCode(1_052_102_000, "辅助核算类别不存在");
    ErrorCode AUXILIARY_TYPE_NAME_DUPLICATE = new ErrorCode(1_052_102_001, "辅助核算类别名称已存在");
    ErrorCode AUXILIARY_TYPE_SYSTEM_NOT_EDITABLE = new ErrorCode(1_052_102_002, "系统预置辅助核算类别不能编辑或删除");
    ErrorCode AUXILIARY_TYPE_HAS_ITEM = new ErrorCode(1_052_102_003,
            "辅助核算类别下有 {} 个项目，不能删除");
    ErrorCode AUXILIARY_ITEM_NOT_EXISTS = new ErrorCode(1_052_102_004, "辅助核算项目不存在");
    ErrorCode AUXILIARY_ITEM_CODE_DUPLICATE = new ErrorCode(1_052_102_005, "辅助核算项目编码已存在");
    ErrorCode AUXILIARY_TYPE_SUBJECT_IN_USE = new ErrorCode(1_052_102_006,
            "辅助核算类别已被 {} 个科目使用，不能删除");
    ErrorCode AUXILIARY_ITEM_VOUCHER_IN_USE = new ErrorCode(1_052_102_007,
            "辅助核算项目已被 {} 条凭证分录使用，不能删除");
    ErrorCode AUXILIARY_ITEM_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_052_102_008,
            "导入辅助核算项目数据不能为空");
    ErrorCode AUXILIARY_ITEM_VOUCHER_TEMPLATE_IN_USE = new ErrorCode(1_052_102_009,
            "辅助核算项目已被 {} 个凭证模板使用，不能删除");
    ErrorCode AUXILIARY_ITEM_INITIAL_BALANCE_IN_USE = new ErrorCode(1_052_102_010,
            "辅助核算项目已被 {} 条初始余额使用，不能删除");
    ErrorCode AUXILIARY_TYPE_VOUCHER_IN_USE = new ErrorCode(1_052_102_012,
            "辅助核算类别已被 {} 条凭证分录使用，不能删除");
    ErrorCode AUXILIARY_TYPE_VOUCHER_TEMPLATE_IN_USE = new ErrorCode(1_052_102_013,
            "辅助核算类别已被 {} 个凭证模板使用，不能删除");
    ErrorCode AUXILIARY_TYPE_INITIAL_BALANCE_IN_USE = new ErrorCode(1_052_102_014,
            "辅助核算类别已被 {} 条初始余额使用，不能删除");

    // ========== 凭证 1-052-103-000 ==========
    ErrorCode VOUCHER_NOT_EXISTS = new ErrorCode(1_052_103_000, "凭证不存在");
    ErrorCode VOUCHER_WORD_NOT_EXISTS = new ErrorCode(1_052_103_001, "凭证字不存在");
    ErrorCode VOUCHER_NUMBER_DUPLICATE = new ErrorCode(1_052_103_002, "该期间的凭证号已存在");
    ErrorCode VOUCHER_ENTRY_REQUIRED = new ErrorCode(1_052_103_003, "凭证至少需要两条有效分录");
    ErrorCode VOUCHER_ENTRY_AMOUNT_INVALID = new ErrorCode(1_052_103_004, "凭证分录只能填写借方或贷方金额");
    ErrorCode VOUCHER_AMOUNT_UNBALANCED = new ErrorCode(1_052_103_005, "凭证借贷金额不平衡");
    ErrorCode VOUCHER_APPROVED_NOT_EDITABLE = new ErrorCode(1_052_103_006, "已审核凭证不能编辑或删除");
    ErrorCode VOUCHER_STATUS_INVALID = new ErrorCode(1_052_103_007, "凭证状态流转不正确");
    ErrorCode VOUCHER_SUBJECT_DISABLED = new ErrorCode(1_052_103_008, "凭证分录使用了已停用科目");
    ErrorCode VOUCHER_SUBJECT_HAS_CHILDREN = new ErrorCode(1_052_103_009, "凭证分录不能使用非末级科目");
    ErrorCode VOUCHER_AUXILIARY_REQUIRED = new ErrorCode(1_052_103_010, "凭证分录的辅助核算项目不完整");
    ErrorCode VOUCHER_QUANTITY_INVALID = new ErrorCode(1_052_103_011, "凭证分录的数量核算信息不正确");
    ErrorCode VOUCHER_WORD_NAME_DUPLICATE = new ErrorCode(1_052_103_012, "凭证字已存在");
    ErrorCode VOUCHER_WORD_DEFAULT_REQUIRED = new ErrorCode(1_052_103_013, "请确保至少有一个默认凭证字");
    ErrorCode VOUCHER_WORD_DEFAULT_NOT_DELETABLE = new ErrorCode(1_052_103_014, "默认凭证字不允许删除");
    ErrorCode VOUCHER_WORD_IN_USE_NOT_EDITABLE = new ErrorCode(1_052_103_015, "使用中的凭证字不能编辑");
    ErrorCode VOUCHER_WORD_IN_USE_NOT_DELETABLE = new ErrorCode(1_052_103_016,
            "该凭证字已被 {} 张凭证使用，不能删除");
    ErrorCode VOUCHER_TEMPLATE_CATEGORY_NOT_EXISTS = new ErrorCode(1_052_103_017, "凭证模板分类不存在");
    ErrorCode VOUCHER_TEMPLATE_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_052_103_018, "凭证模板分类已存在");
    ErrorCode VOUCHER_TEMPLATE_CATEGORY_IN_USE = new ErrorCode(1_052_103_019, "分类下存在凭证模板，不能删除");
    ErrorCode VOUCHER_TEMPLATE_NOT_EXISTS = new ErrorCode(1_052_103_020, "凭证模板不存在");
    ErrorCode DIGEST_NOT_EXISTS = new ErrorCode(1_052_103_021, "常用摘要不存在");
    ErrorCode VOUCHER_STATISTICS_RANGE_INVALID = new ErrorCode(1_052_103_022, "凭证汇总查询范围不正确");
    ErrorCode VOUCHER_MOVE_RANGE_INVALID = new ErrorCode(1_052_103_023, "移动到的凭证号必须小于原凭证号");
    ErrorCode VOUCHER_MOVE_SOURCE_NOT_EXISTS = new ErrorCode(1_052_103_024, "要移动的凭证不存在");
    ErrorCode VOUCHER_IMPORT_FILE_EMPTY = new ErrorCode(1_052_103_025, "请选择要导入的凭证文件");
    ErrorCode VOUCHER_IMPORT_FILE_TOO_LARGE = new ErrorCode(1_052_103_026, "导入文件不能超过 2MB");
    ErrorCode VOUCHER_IMPORT_TEMPLATE_INVALID = new ErrorCode(1_052_103_027, "凭证导入模板格式不正确");
    ErrorCode VOUCHER_CLOSING_GENERATED_NOT_EDITABLE = new ErrorCode(1_052_103_029, "结账生成凭证不能编辑、审核或删除");
    ErrorCode VOUCHER_WORD_IN_USE_BY_CLOSING_SCHEME_NOT_DELETABLE = new ErrorCode(1_052_103_030,
            "该凭证字已被 {} 个结账方案使用，不能删除");

    // ========== 账簿 1-052-104-000 ==========
    ErrorCode LEDGER_PERIOD_INVALID = new ErrorCode(1_052_104_000, "开始会计期间不能晚于结束会计期间");
    ErrorCode INITIAL_BALANCE_SUBJECT_NOT_LEAF = new ErrorCode(1_052_104_001, "初始余额只能录入末级科目");
    ErrorCode INITIAL_BALANCE_AUXILIARY_INVALID = new ErrorCode(1_052_104_002, "辅助核算项目与科目配置不一致");
    ErrorCode INITIAL_BALANCE_PROFIT_LOSS_YEAR_OPENING_INVALID = new ErrorCode(1_052_104_003,
            "损益类科目的年初余额必须为 0");
    ErrorCode INITIAL_BALANCE_IMPORT_TEMPLATE_INVALID = new ErrorCode(1_052_104_006,
            "初始余额导入模板格式不正确");
    ErrorCode INITIAL_BALANCE_IMPORT_ROW_INVALID = new ErrorCode(1_052_104_008,
            "第 {} 行导入失败：{}");
    ErrorCode INITIAL_BALANCE_PERIOD_CLOSED = new ErrorCode(1_052_104_009,
            "账套已结账，不能修改初始余额");
    ErrorCode LEDGER_PERIOD_BEFORE_ACCOUNT_START = new ErrorCode(1_052_104_010,
            "开始会计期间不能早于账套启用期间");

    // ========== 结账 1-052-105-000 ==========
    ErrorCode CLOSING_PERIOD_CLOSED = new ErrorCode(1_052_105_000, "该会计期间已结账");
    ErrorCode CLOSING_PERIOD_NOT_CLOSED = new ErrorCode(1_052_105_001, "该会计期间尚未结账");
    ErrorCode CLOSING_NOT_LATEST_PERIOD = new ErrorCode(1_052_105_002, "只能反结账最近的已结账期间");
    ErrorCode CLOSING_VOUCHER_PENDING_REVIEW = new ErrorCode(1_052_105_003, "存在未审核凭证，不能结账");
    ErrorCode CLOSING_PROFIT_LOSS_NOT_TRANSFERRED = new ErrorCode(1_052_105_004, "损益类科目尚有余额，请先结转损益");
    ErrorCode CLOSING_BALANCE_SHEET_UNBALANCED = new ErrorCode(1_052_105_005, "资产负债表不平衡，不能结账");
    ErrorCode CLOSING_PROFIT_SUBJECT_INVALID = new ErrorCode(1_052_105_006, "本年利润科目不正确");
    ErrorCode CLOSING_NO_PROFIT_LOSS_BALANCE = new ErrorCode(1_052_105_007, "当前期间无需结转损益");
    ErrorCode CLOSING_SCHEME_NOT_EXISTS = new ErrorCode(1_052_105_008, "结账方案不存在");
    ErrorCode CLOSING_SCHEME_SUBJECT_INVALID = new ErrorCode(1_052_105_009, "结账方案科目不正确");
    ErrorCode CLOSING_SCHEME_RATIO_INVALID = new ErrorCode(1_052_105_010, "结账方案借贷金额比例必须分别等于 100%");
    ErrorCode CLOSING_SCHEME_NO_BALANCE = new ErrorCode(1_052_105_011, "当前期间无需生成该结转凭证");
    ErrorCode CLOSING_SCHEME_IN_USE = new ErrorCode(1_052_105_012, "结账方案已生成凭证，不能删除");
    ErrorCode CLOSING_NOT_CURRENT_PERIOD = new ErrorCode(1_052_105_013, "只能操作当前会计期间");
    ErrorCode CLOSING_PROFIT_LOSS_SETTINGS_INVALID = new ErrorCode(1_052_105_014, "结转损益科目设置不正确");
    ErrorCode CLOSING_PROFIT_LOSS_SETTINGS_NOT_EXISTS = new ErrorCode(1_052_105_015, "请先完成结转损益参数设置");
    ErrorCode CLOSING_SPECIAL_SETTINGS_INVALID = new ErrorCode(1_052_105_016, "专用结转设置不正确");
    ErrorCode CLOSING_SPECIAL_SUBJECT_NOT_EXISTS = new ErrorCode(1_052_105_017, "专用结转所需的预置科目不存在");
    ErrorCode CLOSING_INITIAL_BALANCE_UNBALANCED = new ErrorCode(1_052_105_018, "初始余额试算不平衡，不能结账");
    ErrorCode CLOSING_VOUCHER_NUMBER_DISCONTINUOUS = new ErrorCode(1_052_105_019,
            "当前期间存在断号凭证，请先整理凭证号");
    ErrorCode CLOSING_PROFIT_LOSS_VOUCHER_MISSING = new ErrorCode(1_052_105_020,
            "当前期间存在损益发生额，请先生成结转损益凭证");
    ErrorCode CLOSING_INCOME_STATEMENT_UNBALANCED = new ErrorCode(1_052_105_021,
            "利润表勾稽不平衡，不能结账");
    ErrorCode CLOSING_REPORT_SUBJECT_UNMAPPED = new ErrorCode(1_052_105_022,
            "存在未纳入报表公式的科目，不能结账");
    ErrorCode CLOSING_TEMPLATE_NOT_EXISTS = new ErrorCode(1_052_105_023, "结账模板不存在");
    ErrorCode CLOSING_TEMPLATE_PRESET_INVALID = new ErrorCode(1_052_105_024, "结账模板预置数据不正确");
    ErrorCode CLOSING_TEMPLATE_PRESET_SUBJECT_NOT_EXISTS = new ErrorCode(1_052_105_025,
            "结账模板【{}】所需科目【{}】不存在");
    ErrorCode CLOSING_SCHEME_PRESET_SUBJECT_NOT_EXISTS = new ErrorCode(1_052_105_026,
            "结账方案【{}】所需科目【{}】不存在");
    ErrorCode CLOSING_SCHEME_NOT_PERIOD_END = new ErrorCode(1_052_105_027,
            "该结账方案未启用期末结转");

    // ========== 报表 1-052-106-000 ==========
    ErrorCode REPORT_CONFIG_NOT_EXISTS = new ErrorCode(1_052_106_000, "报表项目不存在");
    ErrorCode REPORT_CONFIG_NOT_EDITABLE = new ErrorCode(1_052_106_001, "该报表项目不允许编辑公式");
    ErrorCode REPORT_FORMULA_SUBJECT_DUPLICATE = new ErrorCode(1_052_106_002, "报表公式科目不能重复");
    ErrorCode REPORT_FORMULA_RULE_INVALID = new ErrorCode(1_052_106_003, "报表公式取数规则不正确");
    ErrorCode REPORT_FORMULA_OPERATOR_INVALID = new ErrorCode(1_052_106_004, "报表公式运算符不正确");
    ErrorCode REPORT_ADJUSTMENT_NOT_EXISTS = new ErrorCode(1_052_106_005, "现金流量辅助数据不存在");
    ErrorCode REPORT_AMOUNT_NOT_ADJUSTABLE = new ErrorCode(1_052_106_006, "该现金流量表项目不允许调整金额");

    // ========== 币种 1-052-107-000 ==========
    ErrorCode CURRENCY_NOT_EXISTS = new ErrorCode(1_052_107_000, "币别不存在");
    ErrorCode CURRENCY_CODE_DUPLICATE = new ErrorCode(1_052_107_001, "币别编码已存在");
    ErrorCode CURRENCY_STANDARD_NOT_DELETABLE = new ErrorCode(1_052_107_002, "本位币不允许删除");
    ErrorCode CURRENCY_IN_USE = new ErrorCode(1_052_107_003, "币别已被科目使用，不能删除");

    // ========== 首页 1-052-108-000 ==========
    ErrorCode HOME_METRIC_INVALID = new ErrorCode(1_052_108_001, "首页财务指标不正确");

}
