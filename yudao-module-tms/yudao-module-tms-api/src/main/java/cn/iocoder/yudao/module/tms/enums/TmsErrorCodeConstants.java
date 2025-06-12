package cn.iocoder.yudao.module.tms.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * TMS 错误码枚举类
 * <p>
 * tms 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface TmsErrorCodeConstants {
    // ========== 海关规则 1-030-604-000 ==========
    ErrorCode CUSTOM_RULE_NOT_EXISTS = new ErrorCode(1_030_604_000, "海关规则不存在");
    ErrorCode CUSTOM_RULE_PART_NULL = new ErrorCode(1_030_604_001, "集合中存在部分集合产品名称或供应商产品编码为空");
    ErrorCode NO_REPEAT_OF_COUNTRY_CODE_AND_SUPPLIER_PRODUCT_CODE = new ErrorCode(1_030_604_002, "海关规则中，国家代码+供应商产品编码不能重复");
    ErrorCode NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE = new ErrorCode(1_030_604_003, "海关规则中，产品编码+国家代码({})不能重复");

    // ========== 海关产品分类 1-030-607-000 ==========
    ErrorCode CUSTOM_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_607_001, "海关产品分类表不存在");
    ErrorCode CUSTOM_PRODUCT_EXISTS = new ErrorCode(1_030_607_002, "产品已存在关联，添加失败");

    // ========== 海关分类 1-030-609-000 ==========
    ErrorCode CUSTOM_RULE_CATEGORY_NOT_EXISTS = new ErrorCode(1_030_609_000, "海关分类不存在");
    ErrorCode CUSTOM_RULE_CATEGORY_EXISTS = new ErrorCode(1_030_609_001, "当前报关品名({})的材质已存在，材质+报关品名不可以重复");

    // ========== 海关分类子表 1-030-606-000 ==========
    ErrorCode CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS = new ErrorCode(1_030_606_001, "海关分类子表不存在");
    ErrorCode CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID = new ErrorCode(1_030_606_002, "所选产品中不存在海关分类数据");

    // ========== 头程申请单 1-030-670-000 ==========
    ErrorCode FIRST_MILE_REQUEST_NOT_EXISTS = new ErrorCode(1_030_670_001, "头程申请单(编号:{})不存在");
    ErrorCode FIRST_MILE_REQUEST_ITEM_NOT_EXISTS = new ErrorCode(1_030_670_002, "头程申请单(编号:{})明细不存在");
    ErrorCode FIRST_MILE_REQUEST_OFF_STATUS_NOT_ALLOWED = new ErrorCode(1_030_670_003, "头程申请单({})状态为({})，不允许关闭");
    ErrorCode FIRST_MILE_REQUEST_CODE_DUPLICATE = new ErrorCode(1_030_670_004, "头程申请单编号({})已存在");
    ErrorCode FIRST_MILE_REQUEST_CODE_GENERATE_FAIL = new ErrorCode(1_030_670_005, "头程申请单编号生成失败,大于999999");
    ErrorCode FIRST_MILE_REQUEST_CREATE_FAIL = new ErrorCode(1_030_670_006, "头程申请单创建失败,大于999999");
    ErrorCode FIRST_MILE_REQUEST_AUDIT_STATUS_NOT_ALLOWED = new ErrorCode(1_030_670_007, "头程申请单({})状态为({})，不允许审核");
    ErrorCode FIRST_MILE_REQUEST_ITEM_RELATION_NOT_ALLOWED = new ErrorCode(1_030_670_008, "无法反审核，头程申请单存在关联头程单明细,序号:{}");
    ErrorCode FIRST_MILE_REQUEST_DELETE_FAIL_STATUS_ERROR = new ErrorCode(1_030_670_009, "无法删除，头程申请单存在关联头程单明细,序号:{}");
    ErrorCode FIRST_MILE_REQUEST_UPDATE_FAIL_STATUS_ERROR = new ErrorCode(1_030_670_010, "头程申请单({})已关闭，不允许修改");
    ErrorCode FIRST_MILE_REQUEST_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_670_011, "头程申请单({})状态为({})，不允许修改");
    ErrorCode FIRST_MILE_REQUEST_DELETED_FAIL_APPROVE = new ErrorCode(1_030_670_012, "头程申请单({})状态为({})，不允许删除");
    ErrorCode FIRST_MILE_REQUEST_CODE_FORMAT_ERROR = new ErrorCode(1_030_670_013, "头程申请单({})编号格式错误");
    ErrorCode FIRST_MILE_REQUEST_CODE_DATE_NOT_TODAY = new ErrorCode(1_030_670_014, "头程申请单({})编号日期必须是当天");

    // ========== 状态机异常 1-030-900-000 ==========
    ErrorCode FIRST_MILE_REQUEST_STATUS_MACHINE_ERROR = new ErrorCode(1_030_900_001, "无法在({})状态下触发({})事件");

    // ========== 头程单 1-030-901-000 ==========
    ErrorCode FIRST_MILE_NOT_EXISTS = new ErrorCode(1_030_901_001, "头程单编号({})不存在");
    ErrorCode FIRST_MILE_ID_NOT_EXISTS = new ErrorCode(1_030_901_002, "头程单ID({})不存在");
    ErrorCode FIRST_MILE_CODE_GENERATE_FAIL = new ErrorCode(1_030_901_003, "头程单编号生成失败,大于999999");
    ErrorCode FIRST_MILE_CODE_DUPLICATE = new ErrorCode(1_030_901_004, "头程单编号({})已存在");
    ErrorCode FIRST_MILE_CODE_DATE_NOT_TODAY = new ErrorCode(1_030_901_005, "头程单({})编号日期必须是当天");
    ErrorCode FIRST_MILE_CODE_GENERATE_FAIL_MAX_TRY = new ErrorCode(1_030_901_006, "头程单编号，生成失败，重复{}次");
    ErrorCode FIRST_MILE_CODE_FORMAT_ERROR = new ErrorCode(1_030_901_007, "头程单({})编号格式错误");
    ErrorCode FIRST_MILE_ITEM_LIST_NOT_EMPTY = new ErrorCode(1_030_901_008, "头程单明细列表不能为空");
    ErrorCode FIRST_MILE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_901_009, "头程单({})状态为({})，不允许修改");
    ErrorCode FIRST_MILE_DELETE_FAIL_APPROVE = new ErrorCode(1_030_901_010, "头程单({})状态为({})，不允许删除");
    ErrorCode FIRST_MILE_CREATE_FAIL = new ErrorCode(1_030_901_011, "头程单创建子项失败，原因({})");
    ErrorCode FIRST_MILE_PROCESS_FAIL_WAREHOUSE_ID_DONT_EXISTS = new ErrorCode(1_030_901_012, "创建出库单需要头程明细中仓库ID不为空");
    ErrorCode FIRST_MILE_PROCESS_FAIL_WMS_OUTBOUND_EXISTS = new ErrorCode(1_030_901_013, "头程单创建出库单失败，原因:{}");
    ErrorCode FIRST_MILE_WMS_OUTBOUND_NOT_CAN_ABANDON = new ErrorCode(1_030_901_014, "反审核失败，头程单已生成出库单({}),不处于草稿状态,无法撤销");
    ErrorCode FEE_WMS_OUTBOUND_NOT_CAN_ABANDON = new ErrorCode(1_030_901_015, "作废出库单失败，原因:{}");

    // ========== 费用明细 1-030-902-000 ==========
    ErrorCode FEE_NOT_EXISTS = new ErrorCode(1_030_902_001, "费用不存在,ID={},类型={}");

    // ========== 头程单明细 1-030-903-000 ==========
    ErrorCode FIRST_MILE_ITEM_NOT_EXISTS = new ErrorCode(1_030_903_001, "头程单明细编号({})不存在");

    // ========== 出运跟踪信息 1-030-904-000 ==========
    ErrorCode VESSEL_TRACKING_NOT_EXISTS = new ErrorCode(1_030_904_001, "出运跟踪信息表不存在");
    ErrorCode VESSEL_TRACKING_LOG_NOT_EXISTS = new ErrorCode(1_030_904_002, "出运跟踪信息表日志不存在");

    // ========== 港口信息 1-030-905-000 ==========
    ErrorCode PORT_INFO_NOT_EXISTS = new ErrorCode(1_030_905_001, "TMS港口信息(编号:{})不存在");
    ErrorCode PORT_INFO_NAME_DUPLICATE = new ErrorCode(1_030_905_002, "港口名称{}已存在");

    // ========== 调拨单 1-030-906-000 ==========
    ErrorCode TRANSFER_NOT_EXISTS = new ErrorCode(1_030_906_001, "调拨单{}不存在");
    ErrorCode TRANSFER_CODE_DUPLICATE = new ErrorCode(1_030_906_002, "调拨单编码{}已存在");
    ErrorCode TRANSFER_CREATE_OUT_STOCK_ERROR = new ErrorCode(1_030_906_003, "调拨单创建出库单失败，原因：{}");
    ErrorCode TRANSFER_CREATE_IN_STOCK_ERROR = new ErrorCode(1_030_906_004, "调拨单创建入库单失败，原因：{}");

    // ========== 调拨单明细 1-030-907-000 ==========
    ErrorCode TRANSFER_ITEM_NOT_EXISTS = new ErrorCode(1_030_907_001, "调拨单明细{}不存在");
    ErrorCode TRANSFER_ITEM_REVOKE_FAIL_OUT_STOCK_EXISTS = new ErrorCode(1_030_907_002, "调拨单明细{}撤销失败，调拨单已生成出库单({}),非草稿状态,无法撤销");
}
