package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购退货 DO
 *
 * @author wdy
 */
@TableName(value = "srm_purchase_return")
@KeySequence("srm_purchase_return_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class SrmPurchaseReturnDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 乐观锁
     */
    @Version
    private Integer version;
    /**
     * 采购退货单号
     */
    private String code;
    /**
     * 审批状态
     * <p>
     * 枚举 {@link SrmAuditStatus}
     */
    private Integer auditStatus;
    /**
     * 审核者id
     */
    private Long auditorId;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 审核意见
     */
    private String auditAdvice;
    /**
     * 供应商编号
     * <p>
     * 关联 {@link SrmSupplierDO#getId()}
     */
    private Long supplierId;
    /**
     * 结算账户编号
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 退货时间
     */
    private LocalDateTime returnTime;

    /**
     * 币种编号
     */
    private Long currencyId;
    /**
     * 价税合计
     */
    private BigDecimal grossTotalPrice;

    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 最终合计价格，单位：元
     * <p>
     * totalPrice = totalProductPrice + totalGrossPrice - discountPrice + otherPrice
     */
    private BigDecimal totalPrice;
    /**
     * 总毛重，单位：kg
     */
    private BigDecimal totalWeight;
    /**
     * 总体积,毫米，单位：mm³
     */
    private BigDecimal totalVolume;
    /**
     * 已退款金额，单位：元
     * <p>
     * 目的：和 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO} 结合，记录已支付金额
     */
    private BigDecimal refundPrice;

    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalGrossPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     * <p>
     * discountPrice = (totalProductPrice + totalGrossPrice) * discountPercent
     */
    private BigDecimal discountPrice;
    /**
     * 其它金额，单位：元
     */
    private BigDecimal otherPrice;

    /**
     * 附件地址
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;

    /**
     * 退款状态
     */
    private Integer refundStatus;
    /**
     * 出库状态
     */
    private Integer outboundStatus;
}