package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxTypeEnum;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * HRM 计税规则 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_salary_tax_rule")
@KeySequence("hrm_salary_tax_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryTaxRuleDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 计税规则名称
     */
    private String name;
    /**
     * 计税类型
     *
     * 枚举 {@link HrmSalaryTaxTypeEnum}
     */
    private Integer type;
    /**
     * 是否计税
     */
    private Boolean taxEnabled;
    /**
     * 起征阈值
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal threshold;
    /**
     * 小数位数
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer decimalScale;
    /**
     * 计税周期类型
     *
     * 枚举 {@link HrmSalaryTaxCycleTypeEnum}
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer cycleType;

}
