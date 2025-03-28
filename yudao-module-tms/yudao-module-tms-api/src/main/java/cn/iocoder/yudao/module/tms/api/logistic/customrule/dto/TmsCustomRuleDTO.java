package cn.iocoder.yudao.module.tms.api.logistic.customrule.dto;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 海关规则+产品+海关分类(主子表)
 */
@Data
public class TmsCustomRuleDTO {
    /**
     * 海关规则id
     */
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
    private Integer declaredValueCurrencyCode;
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
    private Integer logisticAttribute;
    /**
     * 条形码
     */
    private String fbaBarCode;

    private ErpProductDTO productDTO;
}
