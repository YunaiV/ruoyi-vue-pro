package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工绩效运行阶段 Response VO")
@Data
public class HrmPerformanceAssessmentStageRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "员工绩效考核编号")
    private Long assessmentId;

    @Schema(description = "阶段类型")
    private Integer type;

    @Schema(description = "处理员工编号")
    private Long handlerEmployeeId;

    @Schema(description = "处理人姓名")
    private String handlerName;

    @Schema(description = "阶段名称")
    private String name;

    @Schema(description = "评分人类型")
    private Integer raterType;

    @Schema(description = "权重")
    private BigDecimal weight;

    @Schema(description = "评分类型")
    private Integer scoringType;

    @Schema(description = "可见内容")
    private Integer visibleContent;

    @Schema(description = "是否必填")
    private Boolean requiredSetting;

    @Schema(description = "驳回权限")
    private Boolean rejectAuthority;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "绩效员工评审阶段状态")
    private Integer status;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "结果等级")
    private String resultLevel;

    @Schema(description = "说明")
    private String comment;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "截止时间")
    private LocalDateTime deadlineTime;

    @Schema(description = "是否可处理")
    private Boolean canHandle;

    @Schema(description = "是否可评分")
    private Boolean canScore;

    @Schema(description = "指标评分列表")
    private List<HrmPerformanceAssessmentQuotaScoreRespVO> quotaScoreList;

}
