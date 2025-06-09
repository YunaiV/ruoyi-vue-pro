package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SRM采购到货表 DO
 */
@TableName(value = "srm_purchase_arrive")
@KeySequence("srm_purchase_arrive_seq") // Oracle、PostgreSQL、Kingbase、DB2、H2 用。MySQL 可省略
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrmPurchaseInDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 乐观锁
     */
    @Version
    private Integer version;

    /**
     * 采购到货单据编号
     */
    private String code;

    // ========= 基础关联字段 =========

    /**
     * 供应商编号
     */
    private Long supplierId;

    /**
     * 结算账户编号
     */
    private Long accountId;

    /**
     * 币别ID
     */
    private Long currencyId;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 收货地址
     */
    private String address;

    // ========= 单据时间 =========

    /**
     * 单据日期
     */
    private LocalDateTime billTime;

    /**
     * 入库时间
     */
    private LocalDateTime arriveTime;

    /**
     * 结算日期
     */
    private LocalDateTime settlementDate;

    // ========= 数量金额字段 =========

    /**
     * 合计数量
     */
    private BigDecimal totalCount;

    /**
     * 最终合计价格（= 产品价格合计 + 税额合计 - 折扣金额 + 其他金额）
     */
    private BigDecimal totalPrice;

    /**
     * 合计产品价格
     */
    private BigDecimal totalProductPrice;

    /**
     * 合计税额
     */
    private BigDecimal totalGrossPrice;


    /**
     * 优惠率（百分比）
     */
    private BigDecimal discountPercent;

    /**
     * 优惠金额
     */
    private BigDecimal discountPrice;

    /**
     * 其他金额
     */
    private BigDecimal otherPrice;

    /**
     * 已支付金额
     */
    private BigDecimal paymentPrice;

    // ========= 审核与对账信息 =========

    /**
     * 付款状态
     */
    private Integer payStatus;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核人ID
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
     * 对账状态（false-未对账，true-已对账）
     */
    private Boolean reconciliationStatus;

    // ========= 附加信息 =========
    /**
     * 入库状态
     */
    private Integer inboundStatus;
    /**
     * 总毛重，单位：kg
     */
    private BigDecimal totalWeight;

    /**
     * 总体积,毫米，单位：mm³
     */
    private BigDecimal totalVolume;

    /**
     * 附件地址
     */
    private String fileUrl;

    /**
     * 备注
     */
    private String remark;
}
