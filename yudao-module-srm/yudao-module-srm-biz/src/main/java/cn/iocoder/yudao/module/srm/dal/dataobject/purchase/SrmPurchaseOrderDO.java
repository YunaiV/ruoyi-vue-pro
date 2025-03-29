package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ERP 采购订单 DO
 *
 * @author 王岽宇
 */
@TableName("erp_purchase_order")
@KeySequence("erp_purchase_order_seq")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrmPurchaseOrderDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 采购单编号
     */
    private String no;
    /**
     * 采购状态
     */
    private Integer status;
    /**
     * 供应商编号
     */
    private Long supplierId;
    /**
     * 结算账户编号
     */
    private Long accountId;
    /**
     * 采购时间
     */
    private LocalDateTime orderTime;
    /**
     * 合计数量
     */
    private BigDecimal totalCount;

    /**
     * 币别名称
     */
    private String currencyName;
    /**
     * 合计价格，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    /**
     * 附件地址
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;
    /**
     * 采购入库数量
     */
    private BigDecimal totalInCount;
    /**
     * 采购退货数量
     */
    private BigDecimal totalReturnCount;
    /**
     * 单据日期
     */
    private LocalDateTime noTime;
    /**
     * 结算日期
     */
    private LocalDateTime settlementDate;
    /**
     * 审核人id
     */
    private Long auditorId;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 财务主体id
     */
    private Long purchaseEntityId;
    /**
     * x码
     */
    private String xCode;
    /**
     * 箱率
     */
    private String containerRate;
    /**
     * 仓库id
     */
    private Long warehouseId;
    /**
     * 开关状态
     */
    private Integer offStatus;
    /**
     * 执行状态
     */
    private Integer executeStatus;
    /**
     * 入库状态
     */
    private Integer inStatus;
    /**
     * 付款状态
     */
    private Integer payStatus;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 收货地址
     */
    private String address;
    /**
     * 付款条款
     */
    private String paymentTerms;
    /**
     * 装运港
     */
    private String portOfLoading;
    /**
     * 目的港
     */
    private String portOfDischarge;
    /**
     * 采购状态
     */
    private Integer orderStatus;
    /**
     * 审核意见
     */
    private String reviewComment;
}