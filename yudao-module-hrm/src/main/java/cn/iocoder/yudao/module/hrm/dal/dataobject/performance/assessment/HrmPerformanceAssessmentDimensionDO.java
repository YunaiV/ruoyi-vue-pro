package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceQuotaTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * HRM 员工绩效考核维度 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_performance_assessment_dimension")
@KeySequence("hrm_performance_assessment_dimension_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmPerformanceAssessmentDimensionDO extends BaseDO {

    /**
     * 考核维度编号
     */
    @TableId
    private Long id;
    /**
     * 员工绩效考核编号
     *
     * 关联 {@link HrmPerformanceAssessmentDO#getId()}
     */
    private Long assessmentId;
    /**
     * 维度名称
     */
    private String name;
    /**
     * 指标类型
     *
     * 枚举 {@link HrmPerformanceQuotaTypeEnum}
     */
    private Integer quotaType;
    /**
     * 维度权重
     */
    private BigDecimal weight;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否允许员工编辑指标
     */
    private Boolean allowEdit;
    /**
     * 排序
     */
    private Integer sort;

}
