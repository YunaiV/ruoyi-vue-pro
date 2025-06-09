package cn.iocoder.yudao.module.srm.enums;

/**
 * SRM模块 - 操作日志常量
 * <p>
 * 常量说明:
 * 1. TYPE: 业务类型常量,用于区分不同业务模块
 * 2. SUB_TYPE: 操作类型常量,用于区分同一业务模块下的不同操作
 * 3. SUCCESS: 操作成功后的日志模板,支持 SpEL 表达式
 * - {{#vo.code}}: 单据编号
 * - {{#businessName}}: 业务名称
 * - {{#codes}}: 多个单据编号
 * - {_DIFF{#vo}}: 变更内容
 * - {{#reqVO.reviewed}}: 是否已审核
 * - {{#reqVO.pass}}: 是否审核通过
 * - {{#enable}}: 是否开启
 * - {{#requestId}}: 请求ID
 * - {{#itemIds}}: 项目ID列表
 */
public interface LogRecordConstants {

    // ======================= 采购申请单 =======================
    /**
     * 采购申请单业务类型
     */
    String SRM_PURCHASE_REQUEST_TYPE = "SRM采购申请单";

    /**
     * 创建采购申请单
     */
    String SRM_PURCHASE_REQUEST_CREATE_SUB_TYPE = "创建采购申请单";
    /**
     * 更新采购申请单
     */
    String SRM_PURCHASE_REQUEST_UPDATE_SUB_TYPE = "更新采购申请单";
    /**
     * 删除采购申请单
     */
    String SRM_PURCHASE_REQUEST_DELETE_SUB_TYPE = "删除采购申请单";
    /**
     * 提交采购申请单审核
     */
    String SRM_PURCHASE_REQUEST_SUBMIT_AUDIT_SUB_TYPE = "提交采购申请单审核";
    /**
     * 审核采购申请单
     */
    String SRM_PURCHASE_REQUEST_AUDIT_SUB_TYPE = "审核采购申请单";
    /**
     * 开启采购申请单
     */
    String SRM_PURCHASE_REQUEST_OPEN_SUB_TYPE = "开启采购申请单";
    /**
     * 关闭采购申请单
     */
    String SRM_PURCHASE_REQUEST_CLOSE_SUB_TYPE = "关闭采购申请单";

    /**
     * 创建采购申请单日志模板
     */
    String SRM_PURCHASE_REQUEST_CREATE_SUCCESS = "创建了采购申请单【{{#vo.code}}】";
    /**
     * 更新采购申请单日志模板
     */
    String SRM_PURCHASE_REQUEST_UPDATE_SUCCESS = "更新了采购申请单【{{#vo.code}}】: {_DIFF{#vo}}";
    /**
     * 删除采购申请单日志模板
     */
    String SRM_PURCHASE_REQUEST_DELETE_SUCCESS = "删除了采购申请单【{{#businessName}}】";
    /**
     * 提交采购申请单审核日志模板
     */
    String SRM_PURCHASE_REQUEST_SUBMIT_AUDIT_SUCCESS = "提交了采购申请单【{{#codes}}】审核";
    /**
     * 开启采购申请单日志模板
     */
    String SRM_PURCHASE_REQUEST_OPEN_SUCCESS = "开启了采购申请单【{{#codes}}】";
    /**
     * 关闭采购申请单日志模板
     */
    String SRM_PURCHASE_REQUEST_CLOSE_SUCCESS = "关闭了采购申请单【{{#codes}}】";

    // ======================= SpEL 表达式常量 =======================
    /**
     * 采购申请单开启/关闭子类型表达式
     * 根据 enable 参数决定使用开启或关闭的子类型
     */
    String SUB_TYPE_SWITCH_EXPRESSION = "#enable ? '开启采购申请单' : '关闭采购申请单'";

    /**
     * 采购申请单开启/关闭成功消息表达式
     * 根据 enable 参数决定使用开启或关闭的消息模板
     */
    String SUCCESS_SWITCH_EXPRESSION = "#enable ? '开启了采购申请单【' + #codes + '】' : '关闭了采购申请单【' + #codes + '】'";

    /**
     * 采购申请单业务编号表达式
     */
    String BIZ_NO_SWITCH_EXPRESSION = "#requestId != null ? #requestId : #itemIds[0]";

    // ======================= 采购订单 =======================
    /**
     * 采购订单业务类型
     */
    String SRM_PURCHASE_ORDER_TYPE = "SRM采购订单";

    /**
     * 创建采购订单
     */
    String SRM_PURCHASE_ORDER_CREATE_SUB_TYPE = "创建采购订单";
    /**
     * 更新采购订单
     */
    String SRM_PURCHASE_ORDER_UPDATE_SUB_TYPE = "更新采购订单";
    /**
     * 删除采购订单
     */
    String SRM_PURCHASE_ORDER_DELETE_SUB_TYPE = "删除采购订单";
    /**
     * 提交采购订单审核
     */
    String SRM_PURCHASE_ORDER_SUBMIT_AUDIT_SUB_TYPE = "提交采购订单审核";
    /**
     * 审核采购订单
     */
    String SRM_PURCHASE_ORDER_AUDIT_SUB_TYPE = "审核采购订单";
    /**
     * 开启采购订单
     */
    String SRM_PURCHASE_ORDER_OPEN_SUB_TYPE = "开启采购订单";
    /**
     * 关闭采购订单
     */
    String SRM_PURCHASE_ORDER_CLOSE_SUB_TYPE = "关闭采购订单";

    /**
     * 创建采购订单日志模板
     */
    String SRM_PURCHASE_ORDER_CREATE_SUCCESS = "创建了采购订单【{{#vo.code}}】";
    /**
     * 更新采购订单日志模板
     */
    String SRM_PURCHASE_ORDER_UPDATE_SUCCESS = "更新了采购订单【{{#vo.code}}】: {_DIFF{#vo}}";
    /**
     * 删除采购订单日志模板
     */
    String SRM_PURCHASE_ORDER_DELETE_SUCCESS = "删除了采购订单【{{#businessName}}】";
    /**
     * 提交采购订单审核日志模板
     */
    String SRM_PURCHASE_ORDER_SUBMIT_AUDIT_SUCCESS = "提交了采购订单【{{#codes}}】审核";

    /**
     * 开启采购订单日志模板
     */
    String SRM_PURCHASE_ORDER_OPEN_SUCCESS = "开启了采购订单【{{#codes}}】";
    /**
     * 关闭采购订单日志模板
     */
    String SRM_PURCHASE_ORDER_CLOSE_SUCCESS = "关闭了采购订单【{{#codes}}】";

    // ======================= SpEL 表达式常量 =======================
    /**
     * 采购订单开启/关闭子类型表达式
     * 根据 open 参数决定使用开启或关闭的子类型
     */
    String ORDER_SUB_TYPE_SWITCH_EXPRESSION = "#open ? '开启采购订单' : '关闭采购订单'";

    /**
     * 采购订单开启/关闭成功消息表达式
     * 根据 open 参数决定使用开启或关闭的消息模板
     */
    String ORDER_SUCCESS_SWITCH_EXPRESSION = "#open ? '开启了采购订单【' + #codes + '】' : '关闭了采购订单【' + #codes + '】'";
    

    // ======================= 采购到货单 =======================
    /**
     * 采购到货单业务类型
     */
    String SRM_PURCHASE_IN_TYPE = "SRM采购到货单";

    /**
     * 创建采购到货单
     */
    String SRM_PURCHASE_IN_CREATE_SUB_TYPE = "创建采购到货单";
    /**
     * 更新采购到货单
     */
    String SRM_PURCHASE_IN_UPDATE_SUB_TYPE = "更新采购到货单";
    /**
     * 删除采购到货单
     */
    String SRM_PURCHASE_IN_DELETE_SUB_TYPE = "删除采购到货单";
    /**
     * 提交采购到货单审核
     */
    String SRM_PURCHASE_IN_SUBMIT_AUDIT_SUB_TYPE = "提交采购到货单审核";
    /**
     * 审核采购到货单
     */
    String SRM_PURCHASE_IN_AUDIT_SUB_TYPE = "审核采购到货单";

    /**
     * 创建采购到货单日志模板
     */
    String SRM_PURCHASE_IN_CREATE_SUCCESS = "创建了采购到货单【{{#vo.code}}】";
    /**
     * 更新采购到货单日志模板
     */
    String SRM_PURCHASE_IN_UPDATE_SUCCESS = "更新了采购到货单【{{#vo.code}}】: {_DIFF{#vo}}";
    /**
     * 删除采购到货单日志模板
     */
    String SRM_PURCHASE_IN_DELETE_SUCCESS = "删除了采购到货单【{{#businessName}}】";
    /**
     * 提交采购到货单审核日志模板
     */
    String SRM_PURCHASE_IN_SUBMIT_AUDIT_SUCCESS = "提交了采购到货单【{{#codes}}】审核";
    /**
     * 审核采购到货单日志模板
     */
    String SRM_PURCHASE_IN_AUDIT_SUCCESS = "{{#reqVO.reviewed ? (#reqVO.pass ? '审核通过' : '审核不通过') : '反审核'}}了采购到货单【{{#vo.code}}】";
} 