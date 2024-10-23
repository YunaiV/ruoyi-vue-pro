package cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule;

import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
public class ErpCustomRuleDO extends BaseDO {

    /**
     * 产品编号
     */
    @TableId
    private Long id;
    /**
     * 国家编码
     */
    private String countryCode;
    /**
     * 类型
     */
    private String type;
    /**
     * 供应商产品编号
     */
    private Long supplierProductId;
    /**
     * 申报品名（英文）
     */
    private String declaredTypeEn;
    /**
     * 申报品名
     */
    private String declaredType;
    /**
     * 申报金额
     */
    private Double declaredValue;
    /**
     * 申报金额币种
     */
    private String declaredValueCurrencyCode;
    /**
     * 税率
     */
    private BigDecimal taxRate;
    /**
     * hs编码
     */
    private String hscode;
    /**
     * 物流属性
     */
    private String logisticAttribute;

}