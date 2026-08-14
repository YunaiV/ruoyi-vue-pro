package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * HRM 员工绩效考核指标评分 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_performance_assessment_quota_score")
@KeySequence("hrm_performance_assessment_quota_score_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmPerformanceAssessmentQuotaScoreDO extends BaseDO {

    /**
     * 考核指标评分编号
     */
    @TableId
    private Long id;
    /**
     * 员工绩效考核阶段编号
     *
     * 关联 {@link HrmPerformanceAssessmentStageDO#getId()}
     */
    private Long assessmentStageId;
    /**
     * 员工绩效考核指标编号
     *
     * 关联 {@link HrmPerformanceAssessmentQuotaDO#getId()}
     */
    private Long assessmentQuotaId;
    /**
     * 指标得分
     */
    private BigDecimal score;
    /**
     * 指标评语
     */
    private String comment;

}
