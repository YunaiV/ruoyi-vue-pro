package cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
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
 * HRM 社保方案项目 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_insurance_scheme_project")
@KeySequence("hrm_insurance_scheme_project_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmInsuranceSchemeProjectDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 社保方案编号
     *
     * 关联 {@link HrmInsuranceSchemeDO#getId()}
     */
    private Long schemeId;
    /**
     * 项目类型
     *
     * 枚举 {@link HrmInsuranceProjectTypeEnum}
     */
    private Integer type;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 缴纳基数
     */
    private BigDecimal baseAmount;
    /**
     * 公司缴纳比例
     */
    private BigDecimal corporateRate;
    /**
     * 个人缴纳比例
     */
    private BigDecimal personalRate;
    /**
     * 公司缴纳金额
     */
    private BigDecimal corporateAmount;
    /**
     * 个人缴纳金额
     */
    private BigDecimal personalAmount;

}
