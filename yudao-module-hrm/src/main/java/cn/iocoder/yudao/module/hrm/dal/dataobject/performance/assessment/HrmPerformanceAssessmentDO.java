package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceConfirmationResultEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentProcessStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceResultAuditStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 员工绩效考核 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_performance_assessment", autoResultMap = true)
@KeySequence("hrm_performance_assessment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmPerformanceAssessmentDO extends BaseDO {

    /**
     * 员工绩效考核编号
     */
    @TableId
    private Long id;
    /**
     * 绩效计划编号
     *
     * 关联 {@link HrmPerformancePlanDO#getId()}
     */
    private Long planId;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 考核状态
     *
     * 枚举 {@link HrmPerformancePlanStatusEnum}
     */
    private Integer status;
    /**
     * 流程处理状态
     *
     * 枚举 {@link HrmPerformanceAssessmentProcessStatusEnum}
     */
    private Integer processStatus;
    /**
     * 当前业务阶段类型
     *
     * 枚举 {@link HrmPerformanceStageTypeEnum}
     */
    private Integer stageType;
    /**
     * 当前阶段排序
     */
    private Integer stageSort;
    /**
     * 综合得分
     */
    private BigDecimal score;
    /**
     * 结果等级
     */
    private String resultLevel;
    /**
     * 绩效系数
     */
    private BigDecimal coefficient;
    /**
     * 目标确认结果
     *
     * 枚举 {@link HrmPerformanceConfirmationResultEnum}
     */
    private Integer targetConfirmationResult;
    /**
     * 目标确认意见
     */
    private String targetConfirmationComment;
    /**
     * 目标确认时间
     */
    private LocalDateTime targetConfirmationTime;
    /**
     * 自评说明
     */
    private String selfComment;
    /**
     * 他评说明
     */
    private String reviewerComment;
    /**
     * 结果说明
     */
    private String resultComment;
    /**
     * 结果确认时间
     */
    private LocalDateTime resultConfirmationTime;
    /**
     * 结果审核状态
     *
     * 枚举 {@link HrmPerformanceResultAuditStatusEnum}
     */
    private Integer resultAuditStatus;
    /**
     * 结果审核时间
     */
    private LocalDateTime resultAuditTime;
    /**
     * 结果审核意见
     */
    private String resultAuditReason;

    /**
     * 申诉原因
     */
    private String appealReason;
    /**
     * 申诉附件地址列表
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> appealFileUrls;
    /**
     * 申诉提交时间
     */
    private LocalDateTime appealSubmitTime;
    /**
     * 申诉状态
     *
     * 枚举 {@link HrmPerformanceAppealStatusEnum}
     */
    private Integer appealStatus;
    /**
     * 申诉完成时间
     */
    private LocalDateTime appealTime;
    /**
     * 申诉处理意见
     */
    private String appealComment;
    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;

}
