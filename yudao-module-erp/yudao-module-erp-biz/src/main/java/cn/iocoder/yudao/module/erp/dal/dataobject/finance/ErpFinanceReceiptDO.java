package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 收款单 DO
 *
 * @author 芋道源码
 */
@TableName("erp_finance_receipt")
@KeySequence("erp_finance_receipt_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceReceiptDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 收款单号
     */
    private String no;
    /**
     * 收款状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.erp.enums.ErpAuditStatus}
     */
    private Integer status;
    /**
     * 收款时间
     */
    private LocalDateTime receiptTime;
    /**
     * 财务人员编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long financeUserId;
    /**
     * 客户编号
     *
     * 关联 {@link ErpCustomerDO#getId()}
     */
    private Long customerId;
    /**
     * 收款账户编号
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
     * receiptPrice = totalPrice - discountPrice
     */
    private BigDecimal receiptPrice;

    /**
     * 备注
     */
    private String remark;

}