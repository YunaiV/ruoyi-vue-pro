package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface ErrorCodeConstants {
    ErrorCode AOP_ENHANCED_EXCEPTION = new ErrorCode(100001, "AOP增强异常");

    // ========== ERP 供应商（1-030-100-000） ==========
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商不存在");
    ErrorCode SUPPLIER_NOT_ENABLE = new ErrorCode(1_030_100_000, "供应商({})未启用");
    ErrorCode SUPPLIER_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商产品不存在");
    ErrorCode SUPPLIER_PRODUCT_CODE_DUPLICATE = new ErrorCode(1_030_500_000, "供应商产品编码已存在");

    // ========== ERP 客户（1-030-200-000）==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_020_200_000, "客户不存在");
    ErrorCode CUSTOMER_NOT_ENABLE = new ErrorCode(1_020_200_001, "客户({})未启用");

    // ========== ERP 销售订单（1-030-201-000） ==========
    ErrorCode SALE_ORDER_NOT_EXISTS = new ErrorCode(1_020_201_000, "销售订单不存在");
    ErrorCode SALE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_020_201_001, "销售订单({})已审核，无法删除");
    ErrorCode SALE_ORDER_PROCESS_FAIL = new ErrorCode(1_020_201_002, "反审核失败，只有已审核的销售订单才能反审核");
    ErrorCode SALE_ORDER_APPROVE_FAIL = new ErrorCode(1_020_201_003, "审核失败，只有未审核的销售订单才能审核");
    ErrorCode SALE_ORDER_NO_EXISTS = new ErrorCode(1_020_201_004, "生成销售单号失败，请重新提交");
    ErrorCode SALE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_201_005, "销售订单({})已审核，无法修改");
    ErrorCode SALE_ORDER_NOT_APPROVE = new ErrorCode(1_020_201_006, "销售订单未审核，无法操作");
    ErrorCode SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED = new ErrorCode(1_020_201_007, "销售订单项({})超过最大允许出库数量({})");
    ErrorCode SALE_ORDER_PROCESS_FAIL_EXISTS_OUT = new ErrorCode(1_020_201_008, "反审核失败，已存在对应的销售出库单");
    ErrorCode SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED = new ErrorCode(1_020_201_009, "销售订单项({})超过最大允许退货数量({})");
    ErrorCode SALE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_020_201_010, "反审核失败，已存在对应的销售退货单");
    ErrorCode SALE_ORDER_NO_OUT_OF_BOUNDS = new ErrorCode(1_020_201_011, "销售订单号编码大于999999,生成失败");

    // ========== ERP 销售出库（1-030-202-000） ==========
    ErrorCode SALE_OUT_NOT_EXISTS = new ErrorCode(1_020_202_000, "销售出库单不存在");
    ErrorCode SALE_OUT_DELETE_FAIL_APPROVE = new ErrorCode(1_020_202_001, "销售出库单({})已审核，无法删除");
    ErrorCode SALE_OUT_PROCESS_FAIL = new ErrorCode(1_020_202_002, "反审核失败，只有已审核的出库单才能反审核");
    ErrorCode SALE_OUT_APPROVE_FAIL = new ErrorCode(1_020_202_003, "审核失败，只有未审核的出库单才能审核");
    ErrorCode SALE_OUT_NO_EXISTS = new ErrorCode(1_020_202_004, "生成出库单失败，请重新提交");
    ErrorCode SALE_OUT_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_202_005, "销售出库单({})已审核，无法修改");
    ErrorCode SALE_OUT_NOT_APPROVE = new ErrorCode(1_020_202_006, "销售出库单未审核，无法操作");
    ErrorCode SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED = new ErrorCode(1_020_202_007, "收款金额({})超过销售出库单总金额({})");
    ErrorCode SALE_OUT_PROCESS_FAIL_EXISTS_RECEIPT = new ErrorCode(1_020_202_008, "反审核失败，已存在对应的收款单");
    ErrorCode SALE_OUT_NO_OUT_OF_BOUNDS = new ErrorCode(1_020_202_009, "销售出库单号编码大于999999,生成失败");

    // ========== ERP 销售退货（1-030-203-000） ==========
    ErrorCode SALE_RETURN_NOT_EXISTS = new ErrorCode(1_020_203_000, "销售退货单不存在");
    ErrorCode SALE_RETURN_DELETE_FAIL_APPROVE = new ErrorCode(1_020_203_001, "销售退货单({})已审核，无法删除");
    ErrorCode SALE_RETURN_PROCESS_FAIL = new ErrorCode(1_020_203_002, "反审核失败，只有已审核的退货单才能反审核");
    ErrorCode SALE_RETURN_APPROVE_FAIL = new ErrorCode(1_020_203_003, "审核失败，只有未审核的退货单才能审核");
    ErrorCode SALE_RETURN_NO_EXISTS = new ErrorCode(1_020_203_004, "生成退货单失败，请重新提交");
    ErrorCode SALE_RETURN_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_203_005, "销售退货单({})已审核，无法修改");
    ErrorCode SALE_RETURN_NOT_APPROVE = new ErrorCode(1_020_203_006, "销售退货单未审核，无法操作");
    ErrorCode SALE_RETURN_FAIL_REFUND_PRICE_EXCEED = new ErrorCode(1_020_203_007, "退款金额({})超过销售退货单总金额({})");
    ErrorCode SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND = new ErrorCode(1_020_203_008, "反审核失败，已存在对应的退款单");
    ErrorCode SALE_RETURN_NO_OUT_OF_BOUNDS = new ErrorCode(1_020_203_009, "销售退货单号编码大于999999,生成失败");

    // ========== ERP 仓库 1-030-400-000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(1_030_400_000, "仓库不存在");
    ErrorCode WAREHOUSE_NOT_ENABLE = new ErrorCode(1_030_400_001, "仓库({})未启用");

    // ========== ERP 其它入库单 1-030-401-000 ==========
    ErrorCode STOCK_IN_NOT_EXISTS = new ErrorCode(1_030_401_000, "其它入库单不存在");
    ErrorCode STOCK_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_401_001, "其它入库单({})已审核，无法删除");
    ErrorCode STOCK_IN_PROCESS_FAIL = new ErrorCode(1_030_401_002, "反审核失败，只有已审核的入库单才能反审核");
    ErrorCode STOCK_IN_APPROVE_FAIL = new ErrorCode(1_030_401_003, "审核失败，只有未审核的入库单才能审核");
    ErrorCode STOCK_IN_NO_EXISTS = new ErrorCode(1_030_401_004, "生成入库单失败，请重新提交");
    ErrorCode STOCK_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_401_005, "其它入库单({})已审核，无法修改");
    ErrorCode STOCK_IN_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_401_006, "入库单号编码大于999999，生成失败");

    // ========== ERP 其它出库单 1-030-402-000 ==========
    ErrorCode STOCK_OUT_NOT_EXISTS = new ErrorCode(1_030_402_000, "其它出库单不存在");
    ErrorCode STOCK_OUT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_402_001, "其它出库单({})已审核，无法删除");
    ErrorCode STOCK_OUT_PROCESS_FAIL = new ErrorCode(1_030_402_002, "反审核失败，只有已审核的出库单才能反审核");
    ErrorCode STOCK_OUT_APPROVE_FAIL = new ErrorCode(1_030_402_003, "审核失败，只有未审核的出库单才能审核");
    ErrorCode STOCK_OUT_NO_EXISTS = new ErrorCode(1_030_402_004, "生成出库单失败，请重新提交");
    ErrorCode STOCK_OUT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_402_005, "其它出库单({})已审核，无法修改");
    ErrorCode STOCK_OUT_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_402_006, "出库单号编码大于999999，生成失败");

    // ========== ERP 库存调拨单 1-030-403-000 ==========
    ErrorCode STOCK_MOVE_NOT_EXISTS = new ErrorCode(1_030_402_000, "库存调拨单不存在");
    ErrorCode STOCK_MOVE_DELETE_FAIL_APPROVE = new ErrorCode(1_030_402_001, "库存调拨单({})已审核，无法删除");
    ErrorCode STOCK_MOVE_PROCESS_FAIL = new ErrorCode(1_030_402_002, "反审核失败，只有已审核的调拨单才能反审核");
    ErrorCode STOCK_MOVE_APPROVE_FAIL = new ErrorCode(1_030_402_003, "审核失败，只有未审核的调拨单才能审核");
    ErrorCode STOCK_MOVE_NO_EXISTS = new ErrorCode(1_030_402_004, "生成调拨号失败，请重新提交");
    ErrorCode STOCK_MOVE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_402_005, "库存调拨单({})已审核，无法修改");
    ErrorCode STOCK_MOVE_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_402_006, "调拨单号编码大于999999，生成失败");

    // ========== ERP 库存盘点单 1-030-403-000 ==========
    ErrorCode STOCK_CHECK_NOT_EXISTS = new ErrorCode(1_030_403_000, "库存盘点单不存在");
    ErrorCode STOCK_CHECK_DELETE_FAIL_APPROVE = new ErrorCode(1_030_403_001, "库存盘点单({})已审核，无法删除");
    ErrorCode STOCK_CHECK_PROCESS_FAIL = new ErrorCode(1_030_403_002, "反审核失败，只有已审核的盘点单才能反审核");
    ErrorCode STOCK_CHECK_APPROVE_FAIL = new ErrorCode(1_030_403_003, "审核失败，只有未审核的盘点单才能审核");
    ErrorCode STOCK_CHECK_NO_EXISTS = new ErrorCode(1_030_403_004, "生成盘点号失败，请重新提交");
    ErrorCode STOCK_CHECK_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_403_005, "库存盘点单({})已审核，无法修改");
    ErrorCode STOCK_CHECK_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_403_006, "盘点单号编码大于999999，生成失败");

    // ========== ERP 产品库存 1-030-404-000 ==========
    ErrorCode STOCK_COUNT_NEGATIVE = new ErrorCode(1_030_404_000, "操作失败，产品({})所在仓库({})的库存：{}，小于变更数量：{}");
    ErrorCode STOCK_COUNT_NEGATIVE2 = new ErrorCode(1_030_404_001, "操作失败，产品({})所在仓库({})的库存不足");

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

    // ========== ERP 海关规则 1-030-608-000 ==========
    ErrorCode CUSTOM_RULE_NOT_EXISTS = new ErrorCode(1_030_608_000, "ERP 海关规则不存在");
    ErrorCode CUSTOM_RULE_PART_NULL = new ErrorCode(1_030_608_001, "集合中存在部分集合产品名称或供应商产品编码为空");
    ErrorCode NO_REPEAT_OF_COUNTRY_CODE_AND_SUPPLIER_PRODUCT_CODE = new ErrorCode(1_030_608_002, "海关规则中，国家代码+供应商产品编码不能重复");
    ErrorCode NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE = new ErrorCode(1_030_608_003, "海关规则中，产品编码+国家代码({})不能重复");

    // ========== 海关分类 1-030-609-000 ==========
    ErrorCode CUSTOM_RULE_CATEGORY_NOT_EXISTS = new ErrorCode(1_030_609_000, "海关分类不存在");

    // ========== 海关分类子表 1-030-610-000 ==========
    ErrorCode CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS = new ErrorCode(1_030_610_000, "海关分类子表不存在");
    ErrorCode CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID = new ErrorCode(1_030_610_001, "所选产品中不存在海关分类数据");

    // ========== Fms财务公司 1-030-611-000 ==========
    ErrorCode FINANCE_SUBJECT_NOT_EXISTS = new ErrorCode(1_030_611_000, "Fms财务公司({})不存在");
    // ========== 海关产品分类表1-030-607-000 ==========
    ErrorCode CUSTOM_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_607_001, "海关产品分类表不存在");
}
