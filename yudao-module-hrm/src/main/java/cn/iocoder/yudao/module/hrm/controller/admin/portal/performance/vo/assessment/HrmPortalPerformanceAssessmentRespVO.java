package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工端绩效考核 Response VO")
@Data
public class HrmPortalPerformanceAssessmentRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "绩效计划编号")
    private Long planId;

    @Schema(description = "绩效计划名称")
    private String name;

    @Schema(description = "考核状态")
    private Integer status;

    @Schema(description = "阶段状态")
    private Integer stageType;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "结果等级")
    private String resultLevel;

    @Schema(description = "绩效系数")
    private BigDecimal coefficient;

    @Schema(description = "结果审核状态")
    private Integer resultAuditStatus;

    @Schema(description = "结果审核时间")
    private LocalDateTime resultAuditTime;

    @Schema(description = "结果审核意见")
    private String resultAuditReason;

    @Schema(description = "申诉原因")
    private String appealReason;

    @Schema(description = "申诉状态")
    private Integer appealStatus;

    @Schema(description = "申诉处理时间")
    private LocalDateTime appealTime;

    @Schema(description = "申诉处理意见")
    private String appealComment;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

    @Schema(description = "归档时间")
    private LocalDateTime archiveTime;

}
