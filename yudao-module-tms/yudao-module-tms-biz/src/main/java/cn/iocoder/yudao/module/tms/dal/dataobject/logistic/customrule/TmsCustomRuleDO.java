package cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 海关规则 DO
 *
 * @author 索迈管理员
 */
@TableName("erp_custom_rule")
@KeySequence("erp_custom_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsCustomRuleDO extends TenantBaseDO {

    /**
     * 海关规则id
     */
    @TableId
    private Long id;
    /**
     * 国家编码
     */
    private Integer countryCode;
    /**
     * 产品id
     */
    private Long productId;
    /**
     * 申报金额
     */
    private Double declaredValue;
    /**
     * 申报金额币种
     */
    private Integer declaredValueCurrencyCode;
    /**
     * 物流属性
     */
    private Integer logisticAttribute;
    /**
     * 条形码
     */
    private String fbaBarCode;
}