package cn.iocoder.yudao.module.tms.service.logistic.customrule.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule.TmsCustomRuleDO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 海关规则 BO 组合海关规则+海关分类主子表信息  海关规则<->海关分类子表
 */
@Data
public class TmsCustomRuleBO extends TmsCustomRuleDO {
    /**
     * 申报品名
     */
    private String declaredType;
    /**
     * 申报品名（英文）
     */
    private String declaredTypeEn;
    /**
     * hs编码
     */
    private String hscode;
    /**
     * 材质-字典
     */
    private Integer material;
    /**
     * 税率
     */
    private BigDecimal taxRate;
}
