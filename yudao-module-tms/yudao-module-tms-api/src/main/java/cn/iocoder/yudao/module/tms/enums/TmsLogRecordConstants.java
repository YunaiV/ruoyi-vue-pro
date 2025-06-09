package cn.iocoder.yudao.module.tms.enums;

/**
 * TMS 模块 - 操作日志常量
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
 */
public interface TmsLogRecordConstants {

    // ========== 业务类型 ==========
    /**
     * 头程单业务类型
     */
    String TMS_FIRST_MILE_TYPE = "TMS头程单";

    // ========== 操作类型 ==========
    /**
     * 创建头程单
     */
    String TMS_FIRST_MILE_CREATE_SUB_TYPE = "创建头程单";
    /**
     * 更新头程单
     */
    String TMS_FIRST_MILE_UPDATE_SUB_TYPE = "更新头程单";
    /**
     * 删除头程单
     */
    String TMS_FIRST_MILE_DELETE_SUB_TYPE = "删除头程单";
    /**
     * 审核头程单
     */
    String TMS_FIRST_MILE_AUDIT_SUB_TYPE = "审核头程单";
    /**
     * 提交头程单审核
     */
    String TMS_FIRST_MILE_SUBMIT_AUDIT_SUB_TYPE = "提交头程单审核";

    // ========== 操作日志 ==========
    /**
     * 创建头程单日志模板
     */
    String TMS_FIRST_MILE_CREATE_SUCCESS = "创建了头程单【{{#vo.code}}】";
    /**
     * 更新头程单日志模板
     */
    String TMS_FIRST_MILE_UPDATE_SUCCESS = "更新了头程单【{{#vo.code}}】: {_DIFF{#vo}}";
    /**
     * 删除头程单日志模板
     */
    String TMS_FIRST_MILE_DELETE_SUCCESS = "删除了头程单【{{#code}}】";
    /**
     * 审核头程单日志模板
     */
    String TMS_FIRST_MILE_AUDIT_SUCCESS = "{{#reqVO.reviewed ? (#reqVO.pass ? '审核通过' : '审核不通过') : '反审核'}}了头程单【{{#vo.code}}】";
    /**
     * 提交头程单审核日志模板
     */
    String TMS_FIRST_MILE_SUBMIT_AUDIT_SUCCESS = "提交了头程单【{{#codes}}】审核";

    // ======================= TMS_FIRST_MILE_REQUEST 头程申请单 =======================
    /**
     * 头程申请单业务类型
     */
    String TMS_FIRST_MILE_REQUEST_TYPE = "TMS头程申请单";
    /**
     * 创建头程申请单
     */
    String TMS_FIRST_MILE_REQUEST_CREATE_SUB_TYPE = "创建头程申请单";
    /**
     * 创建头程申请单日志模板
     */
    String TMS_FIRST_MILE_REQUEST_CREATE_SUCCESS = "创建了头程申请单【{{#vo.code}}】";
    /**
     * 更新头程申请单
     */
    String TMS_FIRST_MILE_REQUEST_UPDATE_SUB_TYPE = "更新头程申请单";
    /**
     * 更新头程申请单日志模板
     */
    String TMS_FIRST_MILE_REQUEST_UPDATE_SUCCESS = "更新了头程申请单【{{#code}}】: {_DIFF{#vo}}";
    /**
     * 删除头程申请单
     */
    String TMS_FIRST_MILE_REQUEST_DELETE_SUB_TYPE = "删除头程申请单";
    /**
     * 删除头程申请单日志模板
     */
    String TMS_FIRST_MILE_REQUEST_DELETE_SUCCESS = "删除了头程申请单【{{#businessName}}】";
    /**
     * 提交头程申请单审核
     */
    String TMS_FIRST_MILE_REQUEST_SUBMIT_AUDIT_SUB_TYPE = "提交头程申请单审核";
    /**
     * 提交头程申请单审核日志模板
     */
    String TMS_FIRST_MILE_REQUEST_SUBMIT_AUDIT_SUCCESS = "提交了头程申请单【{{#codes}}】审核";
    /**
     * 审核头程申请单
     */
    String TMS_FIRST_MILE_REQUEST_AUDIT_SUB_TYPE = "审核头程申请单";
    /**
     * 审核头程申请单日志模板
     */
    String TMS_FIRST_MILE_REQUEST_AUDIT_SUCCESS = "{{#reqVO.reviewed ? (#reqVO.pass ? '审核通过' : '审核不通过') : '反审核'}}了头程申请单【{{#vo.code}}】";
} 