package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - HRM 绩效计划 Response VO")
@Data
public class HrmPerformancePlanRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "绩效计划名称")
    private String name;

    @Schema(description = "考核周期类型")
    private Integer cycleType;

    @Schema(description = "考核周期")
    private String cycle;

    @Schema(description = "季度")
    private Integer quarter;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

    @Schema(description = "考核说明")
    private String description;

    @Schema(description = "考评范围列表")
    private List<Scope> scopes;

    @Schema(description = "考核模板编号")
    private Long assessmentTemplateId;

    @Schema(description = "考核配置快照")
    private AssessmentConfig assessmentConfig;

    @Schema(description = "结果模板编号")
    private Long resultTemplateId;

    @Schema(description = "结果配置快照")
    private ResultConfig resultConfig;

    @Schema(description = "指标设置类型")
    private Integer quotaSettingType;

    @Schema(description = "是否需要目标确认")
    private Boolean targetConfirmation;

    @Schema(description = "目标确认节点")
    private HandlerStage targetConfirmationStage;

    @Schema(description = "评分阶段列表")
    private List<ReviewStage> reviewStages;

    @Schema(description = "是否需要结果审核")
    private Boolean resultAudit;

    @Schema(description = "结果审核节点列表")
    private List<HandlerStage> resultAuditStages;

    @Schema(description = "是否需要结果确认")
    private Boolean resultConfirmation;

    @Schema(description = "申诉处理节点列表")
    private List<HandlerStage> appealStages;

    @Schema(description = "申诉超期天数")
    private Integer appealTimeoutDays;

    @Schema(description = "申诉超期处理方式")
    private Integer appealTimeoutAction;

    @Schema(description = "是否同步薪资")
    private Boolean syncToSalary;

    @Schema(description = "计薪月份")
    private String paidForMonth;

    @Schema(description = "各阶段员工数量")
    private Map<Integer, Long> stageCountMap;

    @Schema(description = "阶段状态")
    private Integer stageType;

    @Schema(description = "绩效计划状态")
    private Integer status;

    @Schema(description = "当前操作阶段")
    private Integer operationType;

    @Schema(description = "终止时间")
    private LocalDateTime terminateTime;

    @Schema(description = "考核模板名称")
    private String assessmentTemplateName;

    @Schema(description = "结果模板名称")
    private String resultTemplateName;

    @Schema(description = "员工数量")
    private Integer employeeCount;

    @Schema(description = "已完成数量")
    private Integer finishedCount;

    @Schema(description = "是否可开始评分")
    private Boolean scoringReady;

    @Schema(description = "是否可开始面谈")
    private Boolean interviewReady;

    @Schema(description = "是否可归档")
    private Boolean archiveReady;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "管理后台 - HRM 绩效计划考评范围 Response VO")
    @Data
    public static class Scope {

        @Schema(description = "范围类型", example = "1")
        private Integer type;

        @Schema(description = "员工编号列表", example = "[1024, 1025]")
        private List<Long> employeeIds;

        @Schema(description = "部门编号列表", example = "[100, 101]")
        private List<Long> deptIds;

        @Schema(description = "聘用形式", example = "1")
        private Integer employeeType;

        @Schema(description = "员工状态列表", example = "[1, 2]")
        private List<Integer> employeeStatuses;
    }

    @Schema(description = "管理后台 - HRM 绩效计划处理节点 Response VO")
    @Data
    public static class HandlerStage {

        @Schema(description = "处理人类型", example = "1")
        private Integer type;

        @Schema(description = "上级或部门层级", example = "1")
        private Integer level;

        @Schema(description = "指定处理员工编号", example = "1024")
        private Long employeeId;
    }

    @Schema(description = "管理后台 - HRM 绩效计划评分阶段 Response VO")
    @Data
    public static class ReviewStage {

        @Schema(description = "评分阶段名称", example = "直属上级评分")
        private String name;

        @Schema(description = "评分人")
        private HandlerStage rater;

        @Schema(description = "评分权重", example = "60")
        private BigDecimal weight;

        @Schema(description = "评分方式", example = "1")
        private Integer scoringType;

        @Schema(description = "评分内容可见范围", example = "1")
        private Integer visibleContent;

        @Schema(description = "评语是否必填", example = "true")
        private Boolean requiredSetting;

        @Schema(description = "是否允许驳回", example = "true")
        private Boolean rejectAuthority;
    }

    @Schema(description = "管理后台 - HRM 绩效考核配置快照 Response VO")
    @Data
    public static class AssessmentConfig {

        @Schema(description = "模板名称", example = "季度绩效模板")
        private String name;

        @Schema(description = "计分方式", example = "1")
        private Integer scoreCalculation;

        @Schema(description = "分数上限类型", example = "1")
        private Integer upperLimitType;

        @Schema(description = "分数上限", example = "100")
        private BigDecimal upperLimitScore;

        @Schema(description = "考核维度配置")
        private List<Dimension> dimensions;

        @Schema(description = "管理后台 - HRM 绩效考核维度 Response VO")
        @Data
        public static class Dimension {

            @Schema(description = "维度名称", example = "工作业绩")
            private String name;

            @Schema(description = "指标类型", example = "1")
            private Integer quotaType;

            @Schema(description = "维度权重", example = "60")
            private BigDecimal weight;

            @Schema(description = "备注", example = "核心业绩指标")
            private String remark;

            @Schema(description = "是否允许员工编辑", example = "true")
            private Boolean allowEdit;

            @Schema(description = "考核指标列表")
            private List<Quota> quotas;
        }

        @Schema(description = "管理后台 - HRM 绩效考核指标 Response VO")
        @Data
        public static class Quota {

            @Schema(description = "指标名称", example = "销售目标达成率")
            private String name;

            @Schema(description = "指标说明", example = "按季度销售目标计算")
            private String illustrate;

            @Schema(description = "评分标准", example = "完成率达到 100% 得满分")
            private String standard;

            @Schema(description = "指标权重", example = "50")
            private BigDecimal weight;

            @Schema(description = "评分类型", example = "1")
            private Integer scoreType;
        }
    }

    @Schema(description = "管理后台 - HRM 绩效结果配置快照 Response VO")
    @Data
    public static class ResultConfig {

        @Schema(description = "模板名称", example = "绩效结果模板")
        private String name;

        @Schema(description = "结果等级配置")
        private List<Level> levels;

        @Schema(description = "管理后台 - HRM 绩效结果等级 Response VO")
        @Data
        public static class Level {

            @Schema(description = "等级名称", example = "优秀")
            private String name;

            @Schema(description = "最低分数", example = "90")
            private BigDecimal minScore;

            @Schema(description = "最高分数", example = "100")
            private BigDecimal maxScore;

            @Schema(description = "绩效系数", example = "1.2")
            private BigDecimal coefficient;
        }
    }

}
