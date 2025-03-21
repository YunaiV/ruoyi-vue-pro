package cn.iocoder.yudao.module.srm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface SrmErrorCodeConstants {
    ErrorCode AOP_ENHANCED_EXCEPTION = new ErrorCode(100001, "AOP增强异常");

    // ========== ERP 供应商（1-030-100-000） ==========
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商不存在");
    ErrorCode SUPPLIER_NOT_ENABLE = new ErrorCode(1_030_100_000, "供应商({})未启用");
    ErrorCode SUPPLIER_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商产品不存在");
    ErrorCode SUPPLIER_PRODUCT_CODE_DUPLICATE = new ErrorCode(1_030_500_000, "供应商产品编码已存在");

    // ========== ERP 采购订单（1-030-101-000） ==========
    ErrorCode PURCHASE_ORDER_NOT_EXISTS = new ErrorCode(1_030_101_000, "采购订单不存在");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_030_101_001, "采购订单({})已审核，无法删除");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL = new ErrorCode(1_030_101_002, "反审核失败，只有已审核的采购订单才能反审核");
    ErrorCode PURCHASE_ORDER_APPROVE_FAIL = new ErrorCode(1_030_101_003, "审核失败，只有未审核的采购订单才能审核");
    ErrorCode PURCHASE_ORDER_NO_EXISTS = new ErrorCode(1_030_101_004, "生成采购单号失败，请重新提交");
    ErrorCode PURCHASE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_101_005, "采购订单({})已审核，无法修改");
    ErrorCode PURCHASE_ORDER_NOT_APPROVE = new ErrorCode(1_030_101_006, "采购订单({})未审核，无法操作");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED = new ErrorCode(1_030_101_007, "采购订单项({})超过最大允许入库数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN = new ErrorCode(1_030_101_008, "反审核失败，已存在对应的采购入库单");
    ErrorCode PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED = new ErrorCode(1_030_101_009, "采购订单项({})超过最大允许退货数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_030_101_010, "反审核失败，已存在对应的采购退货单");
    ErrorCode PURCHASE_ORDER_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_101_011, "采购订单号编码大于999999,生成失败");
    ErrorCode PURCHASE_ORDER_CODE_DUPLICATE = new ErrorCode(1_030_101_012, "采购订单编号({})已存在");
    ErrorCode PURCHASE_ORDER_CLOSE_FAIL = new ErrorCode(1_030_101_014, "未审核的采购订单({})不能进行关闭");
    ErrorCode PURCHASE_ORDER_ITEM_PURCHASE_FAIL_EXCEED = new ErrorCode(1_030_101_015, "采购数量不能大于申请项的剩余订购数量");
    ErrorCode PURCHASE_REQUEST_DELETE_FAIL = new ErrorCode(1_030_101_016, "存在关联的采购订单，不能删除采购申请单");
    ErrorCode PURCHASE_ORDER_ITEM_NOT_EXISTS = new ErrorCode(1_030_101_017, "采购订单项({})不存在");
    ErrorCode PURCHASE_ORDER_ITEM_NOT_OPEN = new ErrorCode(1_030_101_018, "采购订单项({})非开启状态，无法入库");
    ErrorCode PURCHASE_ORDER_IN_ITEM_NOT_OPEN = new ErrorCode(1_030_101_019, "采购订单项({})已完全入库，无法再次入库");
    ErrorCode PURCHASE_ORDER_ITEM_NOT_AUDIT = new ErrorCode(1_030_101_020, "采购订单项({})的订单不是已审核状态，无法合并入库");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL = new ErrorCode(1_030_101_021, "该订单存在关联入库项，无法删除");

    // ========== ERP 采购入库（1-030-102-000） ==========
    ErrorCode PURCHASE_IN_NOT_EXISTS = new ErrorCode(1_030_102_000, "采购入库单不存在");
    ErrorCode PURCHASE_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_102_001, "采购入库单({})已审核，无法删除");
    ErrorCode PURCHASE_IN_PROCESS_FAIL = new ErrorCode(1_030_102_002, "反审核失败，只有已审核的入库单才能反审核");
    ErrorCode PURCHASE_IN_APPROVE_FAIL = new ErrorCode(1_030_102_003, "审核失败，只有未审核的入库单才能审核");
    ErrorCode PURCHASE_IN_NO_EXISTS = new ErrorCode(1_030_102_004, "生成入库单失败，请重新提交");
    ErrorCode PURCHASE_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_102_005, "采购入库单({})已审核，无法修改");
    ErrorCode PURCHASE_IN_NOT_APPROVE = new ErrorCode(1_030_102_006, "采购入库单({})未审核，无法操作");
    ErrorCode PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED = new ErrorCode(1_030_102_007, "付款金额({})超过采购入库单总金额({})");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT = new ErrorCode(1_030_102_008, "反审核失败，已存在对应的付款单");
    ErrorCode PURCHASE_IN_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_101_009, "采购入库单号编码大于999999,生成失败");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_PAYMENT_STATUS = new ErrorCode(1_030_102_010, "反审核失败，已付款入库单不能反审核");
    ErrorCode PURCHASE_IN_ITEM_NOT_EXISTS = new ErrorCode(1_030_102_011, "采购入库单项({})不存在");
    ErrorCode PURCHASE_IN_ITEM_NOT_AUDIT = new ErrorCode(1_030_102_012, "采购入库单项({})关联的入库单未审核，无法创建退货单");
    // ========== ERP 采购退货（1-030-103-000） ==========
    ErrorCode PURCHASE_RETURN_NOT_EXISTS = new ErrorCode(1_030_103_000, "采购退货单({})不存在");
    ErrorCode PURCHASE_RETURN_ITEM_NOT_EXISTS = new ErrorCode(1_030_103_001, "采购退货项({})不存在");
    ErrorCode PURCHASE_RETURN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_103_001, "采购退货单({})已审核，无法删除");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL = new ErrorCode(1_030_103_002, "反审核失败，只有已审核的退货单才能反审核");
    ErrorCode PURCHASE_RETURN_APPROVE_FAIL = new ErrorCode(1_030_103_003, "审核失败，只有未审核的退货单才能审核");
    ErrorCode PURCHASE_RETURN_NO_EXISTS = new ErrorCode(1_030_103_004, "生成退货单失败，请重新提交");
    ErrorCode PURCHASE_RETURN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_103_005, "采购退货单({})已审核，无法修改");
    ErrorCode PURCHASE_RETURN_NOT_APPROVE = new ErrorCode(1_030_103_006, "采购退货单未审核，无法操作");
    ErrorCode PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED = new ErrorCode(1_030_103_007, "退款金额({})超过采购退货单总金额({})");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND = new ErrorCode(1_030_103_008, "反审核失败，已存在对应的退款单");
    ErrorCode PURCHASE_RETURN_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_103_009, "采购退货单号编码大于999999,生成失败");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL_PAYMENT_STATUS = new ErrorCode(1_030_103_010, "已审核的退货单才可以退款");

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


    // ========== ERP 采购申请单 1-030-603-000 ==========
    ErrorCode PURCHASE_REQUEST_NOT_EXISTS = new ErrorCode(1_030_603_100, "采购申请单不存在");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS = new ErrorCode(1_030_603_101, "采购申请项({})不存在");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_FOUND = new ErrorCode(1_030_603_102, "未找到对应的采购申请项,订单项id={},申请项id={}");
    ErrorCode PURCHASE_REQUEST_OPENED = new ErrorCode(1_030_603_110, "采购申请单({})已开启");
    ErrorCode PURCHASE_REQUEST_CLOSED = new ErrorCode(1_030_603_111, "采购申请单({})已关闭");
    ErrorCode PURCHASE_REQUEST_MANUAL_CLOSED = new ErrorCode(1_030_603_112, "采购申请单({})已手动关闭");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_603_120, "采购申请单({})已审核，无法修改");
    ErrorCode PURCHASE_REQUEST_DELETE_FAIL_APPROVE = new ErrorCode(1_030_603_121, "采购申请单({})已审核，无法删除");
    ErrorCode PURCHASE_REQUEST_DELETE_FAIL_CLOSE = new ErrorCode(1_030_603_121, "采购申请单({})已关闭，无法删除");
    ErrorCode PURCHASE_REQUEST_ADD_FAIL_APPROVE = new ErrorCode(1_030_603_122, "采购申请单添加失败");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_REQUEST_ID = new ErrorCode(1_030_603_123, "申请单子表id为({})不属于该采购申请单");
    ErrorCode PURCHASE_REQUEST_ADD_FAIL_PRODUCT = new ErrorCode(1_030_603_124, "采购申请单的产品添加失败");
    ErrorCode PURCHASE_REQUEST_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_603_130, "采购申请单号编码大于999999,生成失败");
    ErrorCode PURCHASE_REQUEST_NO_EXISTS = new ErrorCode(1_030_603_131, "生成采购申请单号失败，请重新提交");
    ErrorCode PURCHASE_REQUEST_PROCESS_FAIL = new ErrorCode(1_030_603_132, "反审核失败，只有已审核的采购申请单才能反审核");
    ErrorCode PURCHASE_REQUEST_PROCESS_FAIL_CLOSE = new ErrorCode(1_030_603_133, "反审核失败，已关闭的采购申请单不能反审核");
    ErrorCode PURCHASE_REQUEST_PROCESS_FAIL_ORDERED = new ErrorCode(1_030_603_134, "反审核失败，已订购的采购申请单不能反审核");
    ErrorCode PURCHASE_REQUEST_APPROVE_FAIL = new ErrorCode(1_030_603_135, "审核失败，只有未审核的采购申请单才能审核");
    ErrorCode PURCHASE_REQUEST_CLOSE_FAIL = new ErrorCode(1_030_603_136, "未审核的采购申请单({})不能进行关闭");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_AUDIT_STATUS = new ErrorCode(1_030_603_137, "非法审核状态变更({})->({})");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_ORDER_STATUS = new ErrorCode(1_030_603_138, "非法采购状态变更({})->({})");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_OFF_STATUS = new ErrorCode(1_030_603_139, "非法开关状态变更({})->({})");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL = new ErrorCode(1_030_603_140, "订单编号({})更新状态失败，请联系管理员");
    ErrorCode PURCHASE_REQUEST_ITEM_CLOSED = new ErrorCode(1_030_603_141, "订单项编号({})已关闭,采购项无法修改");
    ErrorCode PURCHASE_REQUEST_ITEM_MANUAL_CLOSED = new ErrorCode(1_030_603_142, "id({})已手动关闭,采购项无法修改");
    ErrorCode PURCHASE_REQUEST_ITEM_ORDERED = new ErrorCode(1_030_603_143, "申请项({})存在对应的采购订单项，无法反审核");
    //采购子项不存在
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_ID = new ErrorCode(1_030_603_143, "采购请求ID=({})没有子项");
    //当前状态不能触发事件
    ErrorCode PURCHASE_REQUEST_NOT_EXISTS_BY_STATUS = new ErrorCode(1_030_603_144, "无法在当前状态({})下触发事件({})");
    ErrorCode PURCHASE_REQUEST_MERGE_FAIL = new ErrorCode(1_030_603_145, "采购申请单({})未审核，无法合并");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_OPEN = new ErrorCode(1_030_603_146, "采购申请项({})不处于开启状态");
    //采购订单
    // ========== ERP 采购订单 1-030-604-300 ==========

    // ========== ERP 采购入库单 1-030-605-300 ==========
    // ========== ERP 采购退货单 1-030-606-300 ==========
    // ========== ERP 采购支付单 1-030-607-300 ==========

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

    // ========== Erp财务主体 1-030-611-000 ==========
    ErrorCode FINANCE_SUBJECT_NOT_EXISTS = new ErrorCode(1_030_611_000, "Erp财务主体不存在");
    // ========== 海关产品分类表1-030-607-000 ==========
    ErrorCode CUSTOM_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_607_001, "海关产品分类表不存在");
}
