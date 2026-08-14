package cn.iocoder.yudao.module.fms.enums;

/**
 * FMS 操作日志常量
 *
 * @author 芋道源码
 */
public interface LogRecordConstants {

    // ======================= FMS_ACCOUNT_SET 账套 =======================

    String FMS_ACCOUNT_SET_TYPE = "FMS 账套";
    String FMS_ACCOUNT_SET_CREATE_SUB_TYPE = "创建账套";
    String FMS_ACCOUNT_SET_CREATE_SUCCESS = "创建了账套【{{#createReqVO.companyName}}】";
    String FMS_ACCOUNT_SET_UPDATE_SUB_TYPE = "更新账套";
    String FMS_ACCOUNT_SET_UPDATE_SUCCESS =
            "更新了账套【{{#updateReqVO.companyName}}】: {_DIFF{#updateReqVO}}";
    String FMS_ACCOUNT_SET_INITIALIZE_SUB_TYPE = "初始化账套";
    String FMS_ACCOUNT_SET_INITIALIZE_SUCCESS = "初始化了账套【{{#accountSet.companyName}}】";
    String FMS_ACCOUNT_SET_MEMBER_UPDATE_SUB_TYPE = "更新账套成员";
    String FMS_ACCOUNT_SET_MEMBER_UPDATE_SUCCESS =
            "更新了账套【{{#accountSet.companyName}}】的成员授权";

    // ======================= FMS_FINANCE_PARAMETER 财务参数 =======================

    String FMS_FINANCE_PARAMETER_TYPE = "FMS 财务参数";
    String FMS_FINANCE_PARAMETER_UPDATE_SUB_TYPE = "更新财务参数";
    String FMS_FINANCE_PARAMETER_UPDATE_SUCCESS =
            "更新了账套【{{#accountSet.companyName}}】的财务参数: {_DIFF{#updateReqVO}}";

    // ======================= FMS_SUBJECT 科目 =======================

    String FMS_SUBJECT_TYPE = "FMS 科目";
    String FMS_SUBJECT_CREATE_SUB_TYPE = "创建科目";
    String FMS_SUBJECT_CREATE_SUCCESS = "创建了科目【{{#createReqVO.code}} {{#createReqVO.name}}】";
    String FMS_SUBJECT_UPDATE_SUB_TYPE = "更新科目";
    String FMS_SUBJECT_UPDATE_SUCCESS = "更新了科目【{{#updateReqVO.code}} {{#updateReqVO.name}}】";
    String FMS_SUBJECT_DELETE_SUB_TYPE = "删除科目";
    String FMS_SUBJECT_DELETE_SUCCESS = "删除了【{{#subjects.size()}}】个科目";
    String FMS_SUBJECT_STATUS_SUB_TYPE = "更新科目状态";
    String FMS_SUBJECT_STATUS_SUCCESS = "更新了科目状态";

    // ======================= FMS_AUXILIARY 辅助核算 =======================

    String FMS_AUXILIARY_TYPE = "FMS 辅助核算";
    String FMS_AUXILIARY_TYPE_CREATE_SUB_TYPE = "创建辅助核算分类";
    String FMS_AUXILIARY_TYPE_CREATE_SUCCESS = "创建了辅助核算分类【{{#createReqVO.name}}】";
    String FMS_AUXILIARY_TYPE_UPDATE_SUB_TYPE = "更新辅助核算分类";
    String FMS_AUXILIARY_TYPE_UPDATE_SUCCESS = "更新了辅助核算分类【{{#updateReqVO.name}}】";
    String FMS_AUXILIARY_TYPE_DELETE_SUB_TYPE = "删除辅助核算分类";
    String FMS_AUXILIARY_TYPE_DELETE_SUCCESS = "删除了辅助核算分类【{{#auxiliaryType.name}}】";

    String FMS_AUXILIARY_ITEM_TYPE = "FMS 辅助核算项目";
    String FMS_AUXILIARY_ITEM_CREATE_SUB_TYPE = "创建辅助核算项目";
    String FMS_AUXILIARY_ITEM_CREATE_SUCCESS = "创建了辅助核算项目【{{#createReqVO.code}} {{#createReqVO.name}}】";
    String FMS_AUXILIARY_ITEM_UPDATE_SUB_TYPE = "更新辅助核算项目";
    String FMS_AUXILIARY_ITEM_UPDATE_SUCCESS = "更新了辅助核算项目【{{#updateReqVO.code}} {{#updateReqVO.name}}】";
    String FMS_AUXILIARY_ITEM_DELETE_SUB_TYPE = "删除辅助核算项目";
    String FMS_AUXILIARY_ITEM_DELETE_SUCCESS = "删除了辅助核算项目【{{#auxiliaryItem.code}} {{#auxiliaryItem.name}}】";
    String FMS_AUXILIARY_ITEM_STATUS_SUB_TYPE = "更新辅助核算项目状态";
    String FMS_AUXILIARY_ITEM_STATUS_SUCCESS = "更新了辅助核算项目状态";

    // ======================= FMS_VOUCHER 凭证 =======================

    String FMS_VOUCHER_TYPE = "FMS 凭证";
    String FMS_VOUCHER_CREATE_SUB_TYPE = "创建凭证";
    String FMS_VOUCHER_CREATE_SUCCESS = "创建了凭证【{{#voucherId}}】";
    String FMS_VOUCHER_UPDATE_SUB_TYPE = "更新凭证";
    String FMS_VOUCHER_UPDATE_SUCCESS = "更新了凭证【{{#updateReqVO.id}}】";
    String FMS_VOUCHER_ATTACHMENT_UPDATE_SUB_TYPE = "更新凭证附件";
    String FMS_VOUCHER_ATTACHMENT_UPDATE_SUCCESS = "更新了凭证【{{#updateReqVO.id}}】的附件";
    String FMS_VOUCHER_DELETE_SUB_TYPE = "删除凭证";
    String FMS_VOUCHER_DELETE_SUCCESS = "删除了凭证【{{#voucher.id}}】";
    String FMS_VOUCHER_REVIEW_SUB_TYPE = "审核凭证";
    String FMS_VOUCHER_REVIEW_SUCCESS = "更新了凭证审核状态";
    String FMS_VOUCHER_TIDY_SUB_TYPE = "整理凭证";
    String FMS_VOUCHER_TIDY_SUCCESS = "整理了【{{#tidyReqVO.month}}】期间的凭证";
    String FMS_VOUCHER_MOVE_SUB_TYPE = "移动凭证";
    String FMS_VOUCHER_MOVE_SUCCESS = "将【{{#moveReqVO.month}}】期间的【{{#moveReqVO.sourceNumber}}】号凭证移动到【{{#moveReqVO.targetNumber}}】号之前";

    // ======================= FMS_VOUCHER_WORD 凭证字 =======================

    String FMS_VOUCHER_WORD_TYPE = "FMS 凭证字";
    String FMS_VOUCHER_WORD_CREATE_SUB_TYPE = "创建凭证字";
    String FMS_VOUCHER_WORD_CREATE_SUCCESS = "创建了凭证字【{{#createReqVO.name}}】";
    String FMS_VOUCHER_WORD_UPDATE_SUB_TYPE = "更新凭证字";
    String FMS_VOUCHER_WORD_UPDATE_SUCCESS = "更新了凭证字【{{#updateReqVO.name}}】";
    String FMS_VOUCHER_WORD_DELETE_SUB_TYPE = "删除凭证字";
    String FMS_VOUCHER_WORD_DELETE_SUCCESS = "删除了凭证字【{{#voucherWord.name}}】";

    // ======================= FMS_VOUCHER_TEMPLATE 凭证模板 =======================

    String FMS_VOUCHER_TEMPLATE_CATEGORY_TYPE = "FMS 凭证模板分类";
    String FMS_VOUCHER_TEMPLATE_CATEGORY_CREATE_SUB_TYPE = "创建凭证模板分类";
    String FMS_VOUCHER_TEMPLATE_CATEGORY_CREATE_SUCCESS = "创建了凭证模板分类【{{#createReqVO.name}}】";
    String FMS_VOUCHER_TEMPLATE_CATEGORY_UPDATE_SUB_TYPE = "更新凭证模板分类";
    String FMS_VOUCHER_TEMPLATE_CATEGORY_UPDATE_SUCCESS = "更新了凭证模板分类【{{#updateReqVO.name}}】";
    String FMS_VOUCHER_TEMPLATE_CATEGORY_DELETE_SUB_TYPE = "删除凭证模板分类";
    String FMS_VOUCHER_TEMPLATE_CATEGORY_DELETE_SUCCESS = "删除了凭证模板分类【{{#category.name}}】";

    String FMS_VOUCHER_TEMPLATE_TYPE = "FMS 凭证模板";
    String FMS_VOUCHER_TEMPLATE_CREATE_SUB_TYPE = "创建凭证模板";
    String FMS_VOUCHER_TEMPLATE_CREATE_SUCCESS = "创建了凭证模板【{{#createReqVO.name}}】";
    String FMS_VOUCHER_TEMPLATE_UPDATE_SUB_TYPE = "更新凭证模板";
    String FMS_VOUCHER_TEMPLATE_UPDATE_SUCCESS = "更新了凭证模板【{{#updateReqVO.name}}】";
    String FMS_VOUCHER_TEMPLATE_DELETE_SUB_TYPE = "删除凭证模板";
    String FMS_VOUCHER_TEMPLATE_DELETE_SUCCESS = "删除了凭证模板【{{#template.name}}】";

    // ======================= FMS_DIGEST 常用摘要 =======================

    String FMS_DIGEST_TYPE = "FMS 常用摘要";
    String FMS_DIGEST_CREATE_SUB_TYPE = "创建常用摘要";
    String FMS_DIGEST_CREATE_SUCCESS = "创建了常用摘要【{{#createReqVO.content}}】";
    String FMS_DIGEST_UPDATE_SUB_TYPE = "更新常用摘要";
    String FMS_DIGEST_UPDATE_SUCCESS = "更新了常用摘要【{{#updateReqVO.content}}】";
    String FMS_DIGEST_DELETE_SUB_TYPE = "删除常用摘要";
    String FMS_DIGEST_DELETE_SUCCESS = "删除了常用摘要【{{#digest.content}}】";

    // ======================= FMS_CURRENCY 币别 =======================

    String FMS_CURRENCY_TYPE = "FMS 币别";
    String FMS_CURRENCY_CREATE_SUB_TYPE = "创建币别";
    String FMS_CURRENCY_CREATE_SUCCESS = "创建了币别【{{#createReqVO.name}}】";
    String FMS_CURRENCY_UPDATE_SUB_TYPE = "更新币别";
    String FMS_CURRENCY_UPDATE_SUCCESS = "更新了币别【{{#updateReqVO.name}}】";
    String FMS_CURRENCY_DELETE_SUB_TYPE = "删除币别";
    String FMS_CURRENCY_DELETE_SUCCESS = "删除了币别【{{#currency.name}}】";

    // ======================= FMS_INITIAL_BALANCE 财务初始余额 =======================

    String FMS_INITIAL_BALANCE_TYPE = "FMS 财务初始余额";
    String FMS_INITIAL_BALANCE_SAVE_SUB_TYPE = "保存财务初始余额";
    String FMS_INITIAL_BALANCE_SAVE_SUCCESS =
            "保存了账套【{{#saveReqVO.accountSetId}}】的【{{#saveReqVO.balances.size()}}】项财务初始余额";
    String FMS_INITIAL_BALANCE_IMPORT_SUB_TYPE = "导入财务初始余额";
    String FMS_INITIAL_BALANCE_IMPORT_SUCCESS =
            "向账套【{{#accountSetId}}】导入了【{{#_ret}}】项财务初始余额";

    // ======================= FMS_CLOSING 结账 =======================

    String FMS_CLOSING_TYPE = "FMS 结账";
    String FMS_CLOSING_SCHEME_CREATE_SUB_TYPE = "创建结账方案";
    String FMS_CLOSING_SCHEME_CREATE_SUCCESS = "创建了结账方案【{{#createReqVO.name}}】";
    String FMS_CLOSING_SCHEME_UPDATE_SUB_TYPE = "更新结账方案";
    String FMS_CLOSING_SCHEME_UPDATE_SUCCESS = "更新了结账方案【{{#updateReqVO.name}}】";
    String FMS_CLOSING_SCHEME_DELETE_SUB_TYPE = "删除结账方案";
    String FMS_CLOSING_SCHEME_DELETE_SUCCESS = "删除了结账方案【{{#id}}】";
    String FMS_CLOSING_TEMPLATE_CREATE_SUB_TYPE = "创建结账模板";
    String FMS_CLOSING_TEMPLATE_CREATE_SUCCESS = "创建了结账模板【{{#createReqVO.name}}】";
    String FMS_CLOSING_TEMPLATE_UPDATE_SUB_TYPE = "更新结账模板";
    String FMS_CLOSING_TEMPLATE_UPDATE_SUCCESS = "更新了结账模板【{{#updateReqVO.name}}】";
    String FMS_CLOSING_TEMPLATE_DELETE_SUB_TYPE = "删除结账模板";
    String FMS_CLOSING_TEMPLATE_DELETE_SUCCESS = "删除了结账模板【{{#id}}】";
    String FMS_CLOSING_SETTINGS_UPDATE_SUB_TYPE = "更新结账设置";
    String FMS_CLOSING_SETTINGS_UPDATE_SUCCESS = "更新了账套【{{#saveReqVO.accountSetId}}】的结账设置";
    String FMS_CLOSING_SPECIAL_SETTINGS_UPDATE_SUCCESS =
            "更新了账套【{{#updateReqVO.accountSetId}}】的结账方案【{{#updateReqVO.id}}】设置";
    String FMS_CLOSING_VOUCHER_GENERATE_SUB_TYPE = "生成结转凭证";
    String FMS_CLOSING_PROFIT_LOSS_VOUCHER_GENERATE_SUCCESS =
            "生成了账套【{{#generateReqVO.accountSetId}}】{{#generateReqVO.month}}期间的结转损益凭证【{{#_ret}}】";
    String FMS_CLOSING_SCHEME_VOUCHER_GENERATE_SUCCESS =
            "生成了账套【{{#generateReqVO.accountSetId}}】{{#generateReqVO.month}}期间方案【{{#generateReqVO.id}}】的结转凭证【{{#_ret}}】";
    String FMS_CLOSING_VOUCHER_BATCH_GENERATE_SUCCESS =
            "为账套【{{#generateReqVO.accountSetId}}】{{#generateReqVO.month}}期间批量生成了【{{#_ret.size()}}】张结转凭证";
    String FMS_CLOSING_PERIOD_CLOSE_SUB_TYPE = "结账";
    String FMS_CLOSING_PERIOD_CLOSE_SUCCESS =
            "完成了账套【{{#queryReqVO.accountSetId}}】{{#queryReqVO.month}}期间结账";
    String FMS_CLOSING_PERIOD_CANCEL_SUB_TYPE = "反结账";
    String FMS_CLOSING_PERIOD_CANCEL_SUCCESS =
            "完成了账套【{{#queryReqVO.accountSetId}}】{{#queryReqVO.month}}期间反结账";

    // ======================= FMS_CASH_FLOW_STATEMENT 现金流量表 =======================

    String FMS_CASH_FLOW_STATEMENT_TYPE = "FMS 现金流量表";
    String FMS_CASH_FLOW_STATEMENT_UPDATE_SUB_TYPE = "调整现金流量表";
    String FMS_CASH_FLOW_STATEMENT_UPDATE_SUCCESS =
            "调整了账套【{{#updateReqVO.accountSetId}}】{{#updateReqVO.startMonth}} 至 {{#updateReqVO.endMonth}}的现金流量表";
    String FMS_CASH_FLOW_ADJUSTMENT_UPDATE_SUB_TYPE = "调整现金流量辅助数据";
    String FMS_CASH_FLOW_ADJUSTMENT_UPDATE_SUCCESS =
            "调整了账套【{{#updateReqVO.accountSetId}}】的【{{#updateReqVO.items.size()}}】项现金流量辅助数据";
    String FMS_CASH_FLOW_ADJUSTMENT_FORMULA_UPDATE_SUB_TYPE = "更新现金流量辅助公式";
    String FMS_CASH_FLOW_ADJUSTMENT_FORMULA_UPDATE_SUCCESS =
            "更新了账套【{{#updateReqVO.accountSetId}}】的现金流量辅助公式【{{#updateReqVO.id}}】";

}
