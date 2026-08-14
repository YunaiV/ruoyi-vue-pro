package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工绩效指标 Response VO")
@Data
public class HrmPerformanceAssessmentQuotaRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "员工绩效考核编号")
    private Long assessmentId;

    @Schema(description = "员工绩效维度编号")
    private Long dimensionId;

    @Schema(description = "是否允许编辑")
    private Boolean allowEdit;

    @Schema(description = "预设值")
    private Boolean preset;

    @Schema(description = "维度名称")
    private String dimensionName;

    @Schema(description = "指标名称")
    private String name;

    @Schema(description = "指标说明")
    private String description;

    @Schema(description = "标准值")
    private String standard;

    @Schema(description = "维度权重")
    private BigDecimal dimensionWeight;

    @Schema(description = "指标权重")
    private BigDecimal weight;

    @Schema(description = "分数类型")
    private Integer scoreType;

    @Schema(description = "目标值")
    private String targetValue;

    @Schema(description = "实际值")
    private String actualValue;

    @Schema(description = "自评分数")
    private BigDecimal selfScore;

    @Schema(description = "评分人得分")
    private BigDecimal reviewerScore;

    @Schema(description = "最终得分")
    private BigDecimal finalScore;

    @Schema(description = "说明")
    private String comment;

    @Schema(description = "排序")
    private Integer sort;

}
