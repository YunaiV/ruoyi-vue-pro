package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工绩效维度 Response VO")
@Data
public class HrmPerformanceAssessmentDimensionRespVO {

    @Schema(description = "维度编号")
    private Long id;

    @Schema(description = "员工绩效考核编号")
    private Long assessmentId;

    @Schema(description = "维度名称")
    private String name;

    @Schema(description = "指标类型")
    private Integer quotaType;

    @Schema(description = "维度权重")
    private BigDecimal weight;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否允许编辑")
    private Boolean allowEdit;

    @Schema(description = "排序")
    private Integer sort;

}
