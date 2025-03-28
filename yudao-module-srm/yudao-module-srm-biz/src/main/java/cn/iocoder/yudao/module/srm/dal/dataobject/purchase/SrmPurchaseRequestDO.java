package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ERP采购申请单 DO
 *
 * @author 索迈管理员
 */
@TableName("erp_purchase_request")
@KeySequence("erp_purchase_request_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrmPurchaseRequestDO extends TenantBaseDO {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 单据编号
     */
    private String no;
    /**
     * 申请人id
     */
    private Long applicantId;
    /**
     * 申请部门id
     */
    private Long applicationDeptId;
    /**
     * 单据日期
     */
    private LocalDateTime requestTime;
    /**
     * 审核状态
     */
    private Integer status;
//    private Integer status = SrmAuditStatus.DRAFT.getCode(); // 默认草稿;
    /**
     * 关闭状态
     */
    private Integer offStatus;
//    private Integer offStatus = SrmOffStatus.OPEN.getCode(); //默认开启
    /**
     * 订购状态
     */
    private Integer orderStatus;
//    private Integer orderStatus = SrmOrderStatus.OT_ORDERED.getCode(); //默认未订购
    /**
     * 审核者id
     */
    private Long auditorId;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 单据标签
     */
    private String tag;
    /**
     * 供应商id
     */
    private Long supplierId;

    /**
     * 收货地址
     */
    private String delivery;

    /**
     * 审核意见
     */
    private String reviewComment;
    /**
     * 入库状态
     */
    private Integer inStatus;
}