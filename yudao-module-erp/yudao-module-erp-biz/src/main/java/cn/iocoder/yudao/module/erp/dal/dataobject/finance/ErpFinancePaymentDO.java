package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 付款单 DO
 *
 * @author 芋道源码
 */
@TableName("erp_finance_payment")
@KeySequence("erp_finance_payment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinancePaymentDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 付款单号
     */
    private String no;
    /**
     * 付款状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.erp.enums.ErpAuditStatus}
     */
    private Integer status;
    /**
     * 付款时间
     */
    private LocalDateTime paymentTime;
    /**
     * 财务人员编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long financeUserId;
    /**
     * 供应商编号
     *
     * 关联 {@link ErpSupplierDO#getId()}
     */
    private Long supplierId;
    /**
     * 付款账户编号
     *
     * 关联 {@link ErpAccountDO#getId()}
     */
    private Long accountId;

    /**
     * 合计价格，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 优惠金额，单位：元
     */
    private BigDecimal discountPrice;
    /**
     * 实付金额，单位：分
     *
     * paymentPrice = totalPrice - discountPrice
     */
    private BigDecimal paymentPrice;

    /**
     * 备注
     */
    private String remark;

}