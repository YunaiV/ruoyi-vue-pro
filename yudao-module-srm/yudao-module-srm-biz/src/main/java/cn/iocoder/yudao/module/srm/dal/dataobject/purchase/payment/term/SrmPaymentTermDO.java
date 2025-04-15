package cn.iocoder.yudao.module.srm.dal.dataobject.purchase.payment.term;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 付款条款 DO
 *
 * @author wdy
 */
@TableName("srm_payment_term")
@KeySequence("srm_payment_term_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrmPaymentTermDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 人民币采购条款（中文）
     */
    private String paymentTermZh;
    /**
     * 外币采购条款（中文）
     */
    private String paymentTermZhForeign;
    /**
     * 外币采购条款（英文）
     */
    private String paymentTermEnForeign;
    /**
     * 备注
     */
    private String remark;
}