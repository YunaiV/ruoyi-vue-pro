package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaScoreTypeEnum;
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
 * HRM 员工绩效考核指标 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_performance_assessment_quota")
@KeySequence("hrm_performance_assessment_quota_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmPerformanceAssessmentQuotaDO extends BaseDO {

    /**
     * 考核指标编号
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
     * 考核维度编号
     *
     * 关联 {@link HrmPerformanceAssessmentDimensionDO#getId()}
     */
    private Long dimensionId;
    /**
     * 是否预置指标
     */
    private Boolean preset;
    /**
     * 指标名称
     */
    private String name;
    /**
     * 指标说明
     */
    private String description;
    /**
     * 评分标准
     */
    private String standard;
    /**
     * 指标权重
     */
    private BigDecimal weight;
    /**
     * 评分类型
     *
     * 枚举 {@link HrmPerformanceQuotaScoreTypeEnum}
     */
    private Integer scoreType;
    /**
     * 目标值
     */
    private String targetValue;
    /**
     * 实际值
     */
    private String actualValue;
    /**
     * 自评汇总分
     */
    private BigDecimal selfScore;
    /**
     * 他评汇总分
     */
    private BigDecimal reviewerScore;
    /**
     * 最终指标得分
     */
    private BigDecimal finalScore;
    /**
     * 排序
     */
    private Integer sort;

}
