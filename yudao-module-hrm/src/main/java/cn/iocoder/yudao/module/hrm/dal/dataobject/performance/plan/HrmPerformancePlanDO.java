package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan;

import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealTimeoutActionEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanOperationTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanScopeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuarterEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaSettingTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewScoringTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
 * HRM 绩效考核计划 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_performance_plan", autoResultMap = true)
@KeySequence("hrm_performance_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmPerformancePlanDO extends BaseDO {

    /**
     * 计划编号
     */
    @TableId
    private Long id;
    /**
     * 计划状态
     *
     * 枚举 {@link HrmPerformancePlanStatusEnum}
     */
    private Integer status;
    /**
     * 当前业务阶段类型
     *
     * 枚举 {@link HrmPerformanceStageTypeEnum}
     */
    private Integer stageType;
    /**
     * 当前可执行操作类型
     *
     * 枚举 {@link HrmPerformancePlanOperationTypeEnum}
     */
    private Integer operationType;
    /**
     * 终止时间
     */
    private LocalDateTime terminateTime;

    // ==================== 1. 基础设置 ====================

    /**
     * 计划名称
     */
    private String name;
    /**
     * 考核周期类型
     *
     * 枚举 {@link HrmPerformanceCycleTypeEnum}
     */
    private Integer cycleType;
    /**
     * 考核周期
     */
    private String cycle;
    /**
     * 季度
     *
     * 枚举 {@link HrmPerformanceQuarterEnum}
     */
    private Integer quarter;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 考核说明
     */
    private String description;
    /**
     * 考评范围列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Scope> scopes;

    // ==================== 2. 指标设置 ====================

    /**
     * 考核模板编号
     *
     * 关联 {@link HrmPerformanceAssessmentTemplateDO#getId()}
     */
    private Long assessmentTemplateId;
    /**
     * 考核配置快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private HrmPerformanceAssessmentTemplateDO.AssessmentConfig assessmentConfig;

    // ==================== 3. 流程设置 ====================

    /**
     * 指标制定类型
     *
     * 枚举 {@link HrmPerformanceQuotaSettingTypeEnum}
     */
    private Integer quotaSettingType;
    /**
     * 是否开启目标确认
     */
    private Boolean targetConfirmation;
    /**
     * 目标确认节点
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private HandlerStage targetConfirmationStage;
    /**
     * 评分阶段列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ReviewStage> reviewStages;
    /**
     * 是否开启结果审核
     */
    private Boolean resultAudit;
    /**
     * 结果审核节点列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<HandlerStage> resultAuditStages;
    /**
     * 是否开启结果确认
     */
    private Boolean resultConfirmation;
    /**
     * 申诉处理节点列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<HandlerStage> appealStages;
    /**
     * 申诉超期天数
     */
    private Integer appealTimeoutDays;
    /**
     * 申诉超期处理动作
     *
     * 枚举 {@link HrmPerformanceAppealTimeoutActionEnum}
     */
    private Integer appealTimeoutAction;

    // ==================== 4. 结果设置 ====================

    /**
     * 结果模板编号
     *
     * 关联 {@link HrmPerformanceResultTemplateDO#getId()}
     */
    private Long resultTemplateId;
    /**
     * 结果配置快照
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ResultConfig resultConfig;
    /**
     * 是否同步到薪资
     */
    private Boolean syncToSalary;
    /**
     * 计薪月份
     */
    private String paidForMonth;

    /**
     * 绩效计划考评范围
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Scope {

        /**
         * 范围类型
         *
         * 枚举 {@link HrmPerformancePlanScopeTypeEnum}
         */
        private Integer type;

        /**
         * 员工编号列表
         *
         * 关联 {@link HrmEmployeeDO#getId()}
         */
        private List<Long> employeeIds;
        /**
         * 部门编号列表
         *
         * 关联 {@link DeptDO#getId()}
         */
        private List<Long> deptIds;
        /**
         * 聘用形式
         *
         * 枚举 {@link HrmEmployeeTypeEnum}
         */
        private Integer employeeType;
        /**
         * 员工状态列表
         *
         * 枚举 {@link HrmEmployeeStatusEnum}
         */
        private List<Integer> employeeStatuses;
    }

    /**
     * 绩效计划评分阶段
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewStage {

        /**
         * 评分阶段名称
         */
        private String name;
        /**
         * 评分人
         */
        private HandlerStage rater;
        /**
         * 评分权重
         */
        private BigDecimal weight;
        /**
         * 评分方式
         *
         * 枚举 {@link HrmPerformanceReviewScoringTypeEnum}
         */
        private Integer scoringType;
        /**
         * 评分内容可见范围
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
    }

    /**
     * 绩效计划处理节点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandlerStage {

        /**
         * 处理人类型
         *
         * 枚举 {@link HrmPerformanceRaterTypeEnum}
         */
        private Integer type;
        /**
         * 上级或部门层级
         */
        private Integer level;
        /**
         * 指定处理员工编号
         *
         * 关联 {@link HrmEmployeeDO#getId()}
         */
        private Long employeeId;
    }

    /**
     * 绩效结果配置快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultConfig {

        /**
         * 模板名称
         */
        private String name;
        /**
         * 结果等级配置
         */
        private List<Level> levels;
    }

}
