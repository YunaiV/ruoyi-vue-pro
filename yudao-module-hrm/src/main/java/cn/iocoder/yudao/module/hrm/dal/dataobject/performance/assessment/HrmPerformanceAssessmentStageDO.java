package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewScoringTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * HRM 员工绩效考核阶段 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_performance_assessment_stage")
@KeySequence("hrm_performance_assessment_stage_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmPerformanceAssessmentStageDO extends BaseDO {

    /**
     * 考核阶段编号
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
     * 业务阶段类型
     *
     * 枚举 {@link HrmPerformanceStageTypeEnum}
     */
    private Integer type;
    /**
     * 阶段名称
     */
    private String name;
    /**
     * 实际处理员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long handlerEmployeeId;
    /**
     * 评分人类型
     *
     * 枚举 {@link HrmPerformanceRaterTypeEnum}
     */
    private Integer raterType;
    /**
     * 评分权重快照
     */
    private BigDecimal weight;
    /**
     * 评分方式快照
     *
     * 枚举 {@link HrmPerformanceReviewScoringTypeEnum}
     */
    private Integer scoringType;
    /**
     * 评分内容可见范围快照
     *
     * 枚举 {@link HrmPerformanceReviewVisibleContentEnum}
     */
    private Integer visibleContent;
    /**
     * 评语是否必填
     */
    private Boolean requiredSetting;
    /**
     * 是否允许驳回
     */
    private Boolean rejectAuthority;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 阶段处理状态
     *
     * 枚举 {@link HrmPerformanceAssessmentStageStatusEnum}
     */
    private Integer status;
    /**
     * 阶段汇总得分
     */
    private BigDecimal score;
    /**
     * 阶段结果等级
     */
    private String resultLevel;
    /**
     * 阶段意见
     */
    private String comment;
    /**
     * 最近一次驳回原因
     */
    private String rejectReason;
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    /**
     * 截止时间
     */
    private LocalDateTime deadlineTime;

}
