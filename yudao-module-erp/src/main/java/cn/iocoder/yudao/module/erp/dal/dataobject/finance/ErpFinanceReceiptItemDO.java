package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 收款项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_finance_receipt_item")
@KeySequence("erp_finance_receipt_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceReceiptItemDO extends BaseDO {

    /**
     * 入库项编号
     */
    @TableId
    private Long id;
    /**
     * 收款单编号
     *
     * 关联 {@link ErpFinanceReceiptDO#getId()}
     */
    private Long receiptId;

    /**
     * 业务类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum} 的销售出库、退货
     */
    private Integer bizType;
    /**
     * 业务编号
     *
     * 例如说：{@link ErpSaleOutDO#getId()}
     */
    private Long bizId;
    /**
     * 业务单号
     *
     * 例如说：{@link ErpSaleOutDO#getNo()}
     */
    private String bizNo;

    /**
     * 应收金额，单位：分
     */
    private BigDecimal totalPrice;
    /**
     * 已收金额，单位：分
     */
    private BigDecimal receiptedPrice;
    /**
     * 本次收款，单位：分
     */
    private BigDecimal receiptPrice;
    /**
     * 备注
     */
    private String remark;

}