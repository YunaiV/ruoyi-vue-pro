package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 绩效员工 Response VO")
@Data
public class HrmPerformanceAssessmentRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "绩效计划编号")
    private Long planId;

    @Schema(description = "绩效计划名称")
    private String name;

    @Schema(description = "考核周期类型")
    private Integer cycleType;

    @Schema(description = "考核周期")
    private String cycle;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

    @Schema(description = "单项评分上限")
    private BigDecimal upperLimitScore;

    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "部门编号")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "聘用形式")
    private Integer employeeType;

    @Schema(description = "员工状态")
    private Integer employeeStatus;

    @Schema(description = "当前处理人")
    private String currentHandlerName;

    @Schema(description = "考核状态")
    private Integer status;

    @Schema(description = "激活状态")
    private Integer processStatus;

    @Schema(description = "阶段状态")
    private Integer stageType;

    @Schema(description = "阶段排序")
    private Integer stageSort;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "结果等级")
    private String resultLevel;

    @Schema(description = "绩效系数")
    private BigDecimal coefficient;

    @Schema(description = "目标确认员工姓名")
    private String targetConfirmationEmployeeName;

    @Schema(description = "目标确认结果")
    private Integer targetConfirmationResult;

    @Schema(description = "目标确认意见")
    private String targetConfirmationComment;

    @Schema(description = "目标确认时间")
    private LocalDateTime targetConfirmationTime;

    @Schema(description = "是否可确认目标")
    private Boolean canConfirmTarget;

    @Schema(description = "自评说明")
    private String selfComment;

    @Schema(description = "评分人说明")
    private String reviewerComment;

    @Schema(description = "结果说明")
    private String resultComment;

    @Schema(description = "结果确认时间")
    private LocalDateTime resultConfirmationTime;

    @Schema(description = "结果审核状态")
    private Integer resultAuditStatus;

    @Schema(description = "结果审核时间")
    private LocalDateTime resultAuditTime;

    @Schema(description = "结果审核意见")
    private String resultAuditReason;

    @Schema(description = "申诉原因")
    private String appealReason;

    @Schema(description = "申诉附件地址列表")
    private List<String> appealFileUrls;

    @Schema(description = "申诉退回评分阶段编号列表")
    private List<Long> appealReviewStageIds;

    @Schema(description = "申诉提交时间")
    private LocalDateTime appealSubmitTime;

    @Schema(description = "申诉状态")
    private Integer appealStatus;

    @Schema(description = "申诉处理时间")
    private LocalDateTime appealTime;

    @Schema(description = "申诉处理意见")
    private String appealComment;

    @Schema(description = "归档时间")
    private LocalDateTime archiveTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "指标维度列表")
    private List<HrmPerformanceAssessmentDimensionRespVO> dimensions;

    @Schema(description = "指标列表")
    private List<HrmPerformanceAssessmentQuotaRespVO> quotas;

    @Schema(description = "评分阶段列表")
    private List<HrmPerformanceAssessmentStageRespVO> reviewStages;

    @Schema(description = "当前评分阶段")
    private HrmPerformanceAssessmentStageRespVO currentReviewStage;

    @Schema(description = "运行阶段列表")
    private List<HrmPerformanceAssessmentStageRespVO> stages;

    @Schema(description = "当前待处理阶段")
    private HrmPerformanceAssessmentStageRespVO currentStage;

}
