package cn.iocoder.yudao.module.fms.api.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface FmsErrorCodeConstants {
    ErrorCode AOP_ENHANCED_EXCEPTION = new ErrorCode(100001, "AOP增强异常");

    // ========== ERP 产品 1-030-500-000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_030_500_000, "产品不存在");
    ErrorCode PRODUCT_NOT_ENABLE = new ErrorCode(1_030_500_001, "产品({})未启用");
    ErrorCode PRODUCT_CODE_DUPLICATE = new ErrorCode(1_030_500_002, "产品编码已存在");
    ErrorCode DEPT_LEVEL_NOT_MATCH = new ErrorCode(1_030_500_003, "部门等级不符合要求");
    ErrorCode PRODUCT_FIELD_NOT_MATCH = new ErrorCode(1_030_500_004, "传入的字段值和品类实际字段不匹配");
    ErrorCode PRODUCT_NAME_DUPLICATE = new ErrorCode(1_030_500_005, "产品名称已存在");
    ErrorCode PRODUCT_SERIAL_OVER_LIMIT = new ErrorCode(1_030_500_006, "相同颜色、型号、系列的产品个数产过两位数");

    // ========== ERP 产品分类 1-030-501-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_030_501_000, "产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_EXITS_CHILDREN = new ErrorCode(1_030_501_001, "存在存在子产品分类，无法删除");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_EXITS = new ErrorCode(1_030_501_002, "父级产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_ERROR = new ErrorCode(1_030_501_003, "不能设置自己为父产品分类");
    ErrorCode PRODUCT_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_030_501_004, "已经存在该分类名称的产品分类");
    ErrorCode PRODUCT_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_030_501_005, "不能设置自己的子分类为父分类");
    ErrorCode PRODUCT_CATEGORY_EXITS_PRODUCT = new ErrorCode(1_030_502_002, "存在产品使用该分类，无法删除");

    // ========== ERP 产品单位 1-030-502-000 ==========
    ErrorCode PRODUCT_UNIT_NOT_EXISTS = new ErrorCode(1_030_502_000, "产品单位不存在");
    ErrorCode PRODUCT_UNIT_NAME_DUPLICATE = new ErrorCode(1_030_502_001, "已存在该名字的产品单位");
    ErrorCode PRODUCT_UNIT_EXITS_PRODUCT = new ErrorCode(1_030_502_002, "存在产品使用该单位，无法删除");

    // ========== ERP 结算账户 1-030-600-000 ==========
    ErrorCode ACCOUNT_NOT_EXISTS = new ErrorCode(1_030_600_000, "结算账户不存在");
    ErrorCode ACCOUNT_NOT_ENABLE = new ErrorCode(1_030_600_001, "结算账户({})未启用");

    // ========== ERP 付款单 1-030-601-000 ==========
    ErrorCode FINANCE_PAYMENT_NOT_EXISTS = new ErrorCode(1_030_601_000, "付款单不存在");
    ErrorCode FINANCE_PAYMENT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_601_001, "付款单({})已审核，无法删除");
    ErrorCode FINANCE_PAYMENT_PROCESS_FAIL = new ErrorCode(1_030_601_002, "反审核失败，只有已审核的付款单才能反审核");
    ErrorCode FINANCE_PAYMENT_APPROVE_FAIL = new ErrorCode(1_030_601_003, "审核失败，只有未审核的付款单才能审核");
    ErrorCode FINANCE_PAYMENT_NO_EXISTS = new ErrorCode(1_030_601_004, "生成付款单号失败，请重新提交");
    ErrorCode FINANCE_PAYMENT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_601_005, "付款单({})已审核，无法修改");
    ErrorCode FINANCE_PAYMENT_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_601_006, "付款单号编码大于999999，生成失败");

    // ========== ERP 收款单 1-030-602-000 ==========
    ErrorCode FINANCE_RECEIPT_NOT_EXISTS = new ErrorCode(1_030_602_000, "收款单不存在");
    ErrorCode FINANCE_RECEIPT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_602_001, "收款单({})已审核，无法删除");
    ErrorCode FINANCE_RECEIPT_PROCESS_FAIL = new ErrorCode(1_030_602_002, "反审核失败，只有已审核的收款单才能反审核");
    ErrorCode FINANCE_RECEIPT_APPROVE_FAIL = new ErrorCode(1_030_602_003, "审核失败，只有未审核的收款单才能审核");
    ErrorCode FINANCE_RECEIPT_NO_EXISTS = new ErrorCode(1_030_602_004, "生成收款单号失败，请重新提交");
    ErrorCode FINANCE_RECEIPT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_602_005, "收款单({})已审核，无法修改");
    ErrorCode FINANCE_RECEIPT_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_602_006, "收款单号编码大于999999，生成失败");


    // ========== Fms财务公司 1-030-611-000 ==========
    ErrorCode FINANCE_SUBJECT_NOT_EXISTS = new ErrorCode(1_030_611_000, "Fms财务公司({})不存在");
    // ========== 海关产品分类表1-030-607-000 ==========
    ErrorCode CUSTOM_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_607_001, "海关产品分类表不存在");
    ErrorCode CUSTOM_PRODUCT_EXISTS = new ErrorCode(1_030_607_002, "产品已存在关联，添加失败");
}
