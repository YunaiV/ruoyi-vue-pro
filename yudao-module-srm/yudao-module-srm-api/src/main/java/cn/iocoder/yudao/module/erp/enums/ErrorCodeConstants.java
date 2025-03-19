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
    // ========== ERP 产品 1-030-500-000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_030_500_000, "产品不存在");
    ErrorCode PRODUCT_NOT_ENABLE = new ErrorCode(1_030_500_001, "产品({})未启用");
    ErrorCode PRODUCT_CODE_DUPLICATE = new ErrorCode(1_030_500_002, "产品编码已存在");
    ErrorCode DEPT_LEVEL_NOT_MATCH = new ErrorCode(1_030_500_003, "部门等级不符合要求");
    ErrorCode PRODUCT_FIELD_NOT_MATCH = new ErrorCode(1_030_500_004, "传入的字段值和品类实际字段不匹配");
    ErrorCode PRODUCT_NAME_DUPLICATE = new ErrorCode(1_030_500_005, "产品名称已存在");
    ErrorCode PRODUCT_SERIAL_OVER_LIMIT = new ErrorCode(1_030_500_006, "相同颜色、型号、系列的产品个数产过两位数");

    // ========== ERP 产品单位 1-030-502-000 ==========
    ErrorCode PRODUCT_UNIT_NOT_EXISTS = new ErrorCode(1_030_502_000, "产品单位不存在");
    ErrorCode PRODUCT_UNIT_NAME_DUPLICATE = new ErrorCode(1_030_502_001, "已存在该名字的产品单位");
    ErrorCode PRODUCT_UNIT_EXITS_PRODUCT = new ErrorCode(1_030_502_002, "存在产品使用该单位，无法删除");

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


    // ========== ERP 采购申请单 1-030-603-000 ==========
    ErrorCode PURCHASE_REQUEST_NOT_EXISTS = new ErrorCode(1_030_603_100, "采购申请单不存在");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS = new ErrorCode(1_030_603_101, "采购申请项({})不存在");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_FOUND = new ErrorCode(1_030_603_102, "未找到对应的采购申请项,订单项id={},申请项id={}");
    ErrorCode PURCHASE_REQUEST_OPENED = new ErrorCode(1_030_603_110, "采购申请单({})已开启");
    ErrorCode PURCHASE_REQUEST_CLOSED = new ErrorCode(1_030_603_111, "采购申请单({})已关闭");
    ErrorCode PURCHASE_REQUEST_MANUAL_CLOSED = new ErrorCode(1_030_603_112, "采购申请单({})已手动关闭");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_603_120, "采购申请单({})已审核，无法修改");
    ErrorCode PURCHASE_REQUEST_DELETE_FAIL_APPROVE = new ErrorCode(1_030_603_121, "采购申请单({})已审核，无法删除");
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
    //采购子项不存在
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_ID = new ErrorCode(1_030_603_143, "采购请求ID=({})没有子项");
    //当前状态不能触发事件
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_STATUS = new ErrorCode(1_030_603_144, "无法在当前状态({})下触发事件({})");
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
