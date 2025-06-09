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
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商{}不存在");
    ErrorCode SUPPLIER_NOT_ENABLE = new ErrorCode(1_030_100_000, "供应商({})未启用");
    ErrorCode SUPPLIER_PRODUCT_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商产品不存在");
    ErrorCode SUPPLIER_PRODUCT_CODE_DUPLICATE = new ErrorCode(1_030_500_000, "供应商产品编码已存在");
    ErrorCode SUPPLIER_NAME_DUPLICATE = new ErrorCode(1_030_100_001, "供应商名称({})已存在");
    ErrorCode PURCHASE_RETURN_IN_SUPPLIER_NOT_SAME = new ErrorCode(1002013001, "退货项对应的入库单供应商不一致，基准供应商为【{}】，入库单【{}】的供应商为【{}】");
    // ========== ERP 采购订单（1-030-101-000） ==========
    ErrorCode PURCHASE_ORDER_NOT_EXISTS = new ErrorCode(1_030_101_000, "采购订单不存在");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_030_101_001, "采购订单({})已审核，无法删除");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL = new ErrorCode(1_030_101_002, "反审核失败，只有已审核的采购订单才能反审核");
    ErrorCode PURCHASE_ORDER_APPROVE_FAIL = new ErrorCode(1_030_101_003, "审核失败，只有未审核的采购订单才能审核");
    ErrorCode PURCHASE_ORDER_NO_EXISTS = new ErrorCode(1_030_101_004, "生成采购单编号{}失败，请重新提交");
    ErrorCode PURCHASE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_101_005, "采购订单({})当前状态({})，无法修改");
    ErrorCode PURCHASE_ORDER_NOT_APPROVE = new ErrorCode(1_030_101_006, "采购订单({})未审核，无法操作");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED = new ErrorCode(1_030_101_007, "采购订单项({})超过最大允许到货数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN = new ErrorCode(1_030_101_008, "反审核失败，已存在对应的采购到货单");
    ErrorCode PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED = new ErrorCode(1_030_101_009, "采购订单项({}),产品({})超过最大允许退货数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_030_101_010, "反审核失败，已存在对应的采购退货单");
    ErrorCode PURCHASE_ORDER_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_101_011, "采购订单号编码大于999999,生成失败");
    ErrorCode PURCHASE_ORDER_CODE_DUPLICATE = new ErrorCode(1_030_101_012, "采购订单编号({})已存在");
    ErrorCode PURCHASE_ORDER_CLOSE_FAIL = new ErrorCode(1_030_101_014, "未审核的采购订单({})不能进行关闭");
    ErrorCode PURCHASE_ORDER_ITEM_PURCHASE_FAIL_EXCEED = new ErrorCode(1_030_101_015, "采购数量不能大于申请项编号({})的剩余订购数量({})");
    ErrorCode PURCHASE_REQUEST_DELETE_FAIL = new ErrorCode(1_030_101_016, "存在关联的采购订单，不能删除采购申请单");
    ErrorCode PURCHASE_REQUEST_CLOSE_FAIL = new ErrorCode(1_030_101_014, "未审核的采购申请单不能进行({})");
    ErrorCode PURCHASE_ORDER_ITEM_NOT_EXISTS = new ErrorCode(1_030_101_017, "采购订单项编号({})不存在");
    ErrorCode PURCHASE_ORDER_ITEM_NOT_OPEN = new ErrorCode(1_030_101_018, "采购订单项({})非开启状态,无法合并");
    ErrorCode PURCHASE_ORDER_IN_ITEM_NOT_OPEN = new ErrorCode(1_030_101_019, "采购订单项({})已完全到货，无法再次加入到货项");
    ErrorCode PURCHASE_ORDER_ITEM_NOT_AUDIT = new ErrorCode(1_030_101_020, "采购订单项({})的订单不是已审核状态，无法合并");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL = new ErrorCode(1_030_101_021, "该订单存在关联到货项，无法删除");
    ErrorCode PURCHASE_ORDER_GENERATE_CONTRACT_FAIL = new ErrorCode(1_030_101_022, "获取采购合同模板({})失败,({})");
    ErrorCode PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_PARSE = new ErrorCode(1_030_101_022, "编译模板({})失败,({})");
    ErrorCode PURCHASE_ORDER_GENERATE_CONTRACT_FAIL_ERROR = new ErrorCode(1_030_101_022, "生成({})合同发生错误，请联系管理员,({})");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_EXISTS_IN = new ErrorCode(1_030_101_021, "采购订单项编号({})存在对应的到货项，无法反审核");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_EXISTS_DEL = new ErrorCode(1_030_101_021, "采购订单项({})存在对应的采购到货项，无法删除");
    ErrorCode PURCHASE_ORDER_NO_HAS_EXISTS = new ErrorCode(1_030_101_004, "采购订单编号:({})已经存在");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_APPROVE = new ErrorCode(1_030_101_023, "采购订单项({})不处于已审核，无法修改验货+完工单");
    ErrorCode PURCHASE_ORDER_NOT_AUDIT = new ErrorCode(1_030_101_024, "订单处于已审核,才可以生成采购合同");
    ErrorCode PURCHASE_ORDER_UPDATE_FAIL_OFF = new ErrorCode(1_030_101_025, "采购订单({})非开启状态，无法修改");
    ErrorCode PURCHASE_IN_ITEM_QTY_EXCEED = new ErrorCode(1_030_102_001, "采购订单项({})的产品({})剩余可到货量({}) < 输入到货量({})");
    ErrorCode PURCHASE_ORDER_MERGE_IN_FAIL = new ErrorCode(1_030_101_026, "({})合并失败,错误:{}");

    // ========== ERP 采购到货（1-030-102-000） ==========
    ErrorCode PURCHASE_IN_NOT_EXISTS = new ErrorCode(1_030_102_000, "采购到货单不存在");
    ErrorCode PURCHASE_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_102_001, "采购到货单({})处于草稿、未通过、审核撤销，才可以删除");
    ErrorCode PURCHASE_IN_PROCESS_FAIL = new ErrorCode(1_030_102_002, "反审核失败，只有已审核的到货单才能反审核");
    ErrorCode PURCHASE_IN_APPROVE_FAIL = new ErrorCode(1_030_102_003, "审核失败，只有未审核的到货单才能审核");
    ErrorCode PURCHASE_IN_NO_EXISTS = new ErrorCode(1_030_102_004, "生成到货单失败，请重新提交");
    ErrorCode PURCHASE_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_102_005, "采购到货单({})当前({})状态无法修改");
    ErrorCode PURCHASE_IN_NOT_APPROVE = new ErrorCode(1_030_102_006, "采购到货单({})未审核，无法操作");
    ErrorCode PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED = new ErrorCode(1_030_102_007, "付款金额({})超过采购到货单总金额({})");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT = new ErrorCode(1_030_102_008, "反审核失败，已存在对应的付款单");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_PAYMENT_STATUS = new ErrorCode(1_030_102_010, "反审核失败，已付款到货单不能反审核");
    ErrorCode PURCHASE_IN_ITEM_NOT_EXISTS = new ErrorCode(1_030_102_011, "采购到货单项({})不存在");
    ErrorCode PURCHASE_IN_ITEM_NOT_AUDIT = new ErrorCode(1_030_102_012, "采购到货单项({})关联的到货单未审核，无法创建退货单");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_RETURN_ITEM_EXISTS = new ErrorCode(1_030_102_013, "反审核失败，入库项{}已存在对应的退货单");
    ErrorCode PURCHASE_IN_DELETE_FAIL = new ErrorCode(1_030_102_014, "入库项{}存在关联退货项，无法删除");
    ErrorCode PURCHASE_IN_CODE_FORMAT_ERROR = new ErrorCode(1_030_102_015, "采购到货单编号格式不正确");
    ErrorCode PURCHASE_IN_CODE_NOT_TODAY = new ErrorCode(1_030_102_016, "采购到货单编号必须是当天的日期");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_IN_BOUND_EXISTS = new ErrorCode(1_030_102_017, "反审核失败，到货项{}已存在已入库的入库单");
    ErrorCode PURCHASE_IN_ITEM_ORDER_ITEM_NOT_AUDIT_PASS = new ErrorCode(1_030_102_018, "订单项({})关联的到货单({})未审核，无法创建");
    ErrorCode PURCHASE_IN_FAIL_PAYMENT_ITEM_PRICE_EXCEED = new ErrorCode(1_030_102_019, "付款金额({})超过采购入库项总金额({})");
    ErrorCode PURCHASE_IN_NO_OUT_OF_BOUNDS = new ErrorCode(1_030_102_020, "采购到货单号编码大于999999,生成失败");
    ErrorCode PURCHASE_IN_ITEM_CURRENCY_NOT_MATCH = new ErrorCode(1_030_102_021, "采购订单项编号[{}]的币种[{}]与基准币种[{}]不一致");
    ErrorCode PURCHASE_IN_ITEM_COMPANY_NOT_MATCH = new ErrorCode(1_030_102_022, "到货订单编号({})存在多个供应商公司");

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
    ErrorCode PURCHASE_RETURN_IN_ITEM_IN_ID_NOT_SAME = new ErrorCode(1_030_103_011, "退货项中存在多个入库单的入库项");
    ErrorCode PURCHASE_RETURN_WMS_OUTBOUND_NOT_CAN_ABANDON = new ErrorCode(1_030_103_012, "反审核失败，退货单已生成出库单({}),非草稿状态,无法撤销");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL_WMS_OUTBOUND_EXISTS = new ErrorCode(1_030_103_013, "创建出库单失败，原因:{}");
    ErrorCode PURCHASE_RETURN_QTY_EXCEED_IN_QTY = new ErrorCode(1_030_103_015, "退货数量({})超过入库数量({})");
    //PURCHASE_RETURN_WAREHOUSE_NOT_EXISTS
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL_WAREHOUSE_ID_DONT_EXISTS = new ErrorCode(1_030_103_014, "创建出库单需要退货明细中仓库ID不为空");

    // ========== ERP 采购申请单 1-030-603-000 ==========
    ErrorCode PURCHASE_REQUEST_NOT_EXISTS = new ErrorCode(1_030_603_100, "采购申请单不存在");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS = new ErrorCode(1_030_603_101, "采购申请明细行编号({})不存在");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_FOUND = new ErrorCode(1_030_603_102, "未找到对应的采购申请项,订单项编号={},申请项编号={}");
    ErrorCode PURCHASE_REQUEST_OPENED = new ErrorCode(1_030_603_110, "采购申请单({})已开启");
    ErrorCode PURCHASE_REQUEST_CLOSED = new ErrorCode(1_030_603_111, "采购申请单({})已关闭");
    ErrorCode PURCHASE_REQUEST_MANUAL_CLOSED = new ErrorCode(1_030_603_112, "采购申请单({})已手动关闭");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_603_120, "采购申请单({})当前状态({})，无法修改");
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
    ErrorCode PURCHASE_REQUEST_CLOSE_HAS_FAIL = new ErrorCode(1_030_603_136, "未审核的采购申请单({})不能进行关闭");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_AUDIT_STATUS = new ErrorCode(1_030_603_137, "非法审核状态变更({})->({})");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_ORDER_STATUS = new ErrorCode(1_030_603_138, "非法采购状态变更({})->({})");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL_OFF_STATUS = new ErrorCode(1_030_603_139, "非法开关状态变更({})->({})");
    ErrorCode PURCHASE_REQUEST_UPDATE_FAIL = new ErrorCode(1_030_603_140, "订单编号({})更新状态失败，请联系管理员");
    ErrorCode PURCHASE_REQUEST_ITEM_CLOSED = new ErrorCode(1_030_603_141, "订单项编号({})已关闭,采购项无法修改");
    ErrorCode PURCHASE_REQUEST_ITEM_MANUAL_CLOSED = new ErrorCode(1_030_603_142, "id({})已手动关闭,采购项无法修改");
    ErrorCode PURCHASE_REQUEST_ITEM_ORDERED = new ErrorCode(1_030_603_143, "申请项编号({})存在对应的采购订单项，无法反审核");
    //采购子项不存在
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_ID = new ErrorCode(1_030_603_143, "采购请求ID=({})没有子项");
    //当前状态不能触发事件
    //状态机错误回调
    ErrorCode PURCHASE_REQUEST_NOT_EXISTS_BY_EVENT = new ErrorCode(1_030_603_144, "{}无法在({})状态下触发({})事件");
    ErrorCode PURCHASE_REQUEST_MERGE_FAIL = new ErrorCode(1_030_603_145, "采购申请单({})未审核，无法合并");
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_OPEN = new ErrorCode(1_030_603_146, "采购申请项编号({})不处于开启状态");
    //存在对应订单，无法手动关闭
    ErrorCode PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_MANUAL_CLOSE = new ErrorCode(1_030_603_147, "申请单存在关联订单,无法手动关闭状态");
    ErrorCode PURCHASE_REQUEST_NO_EXISTS_BY_NO = new ErrorCode(1_030_603_148, "采购申请单号({})已存在");
    ErrorCode PURCHASE_REQUEST_MERGE_FAIL_REASON = new ErrorCode(1_030_603_149, "采购申请单合并失败，原因:{}");

    // ========== ERP 采购入库单 1-030-605-300 ==========
    // ========== ERP 采购退货单 1-030-606-300 ==========
    // ========== ERP 采购支付单 1-030-607-300 ==========
    // ========== 付款条款  ==========
    ErrorCode PAYMENT_TERM_NOT_EXISTS = new ErrorCode(1_030_607_001, "付款条款不存在");

    // ========== 采购到货单 1002016000-1002016999 ==========
    ErrorCode PURCHASE_IN_ORDER_SUPPLIER_NOT_SAME = new ErrorCode(1002016000, "采购到货单明细关联的采购订单供应商不一致，明细项：{}，供应商：{}，采购订单：{}");
    ErrorCode PURCHASE_IN_ORDER_COMPANY_NOT_SAME = new ErrorCode(1002016001, "采购到货单明细关联的采购订单采购公司不一致，明细项：{}，采购公司：{}，采购订单：{}");

}
