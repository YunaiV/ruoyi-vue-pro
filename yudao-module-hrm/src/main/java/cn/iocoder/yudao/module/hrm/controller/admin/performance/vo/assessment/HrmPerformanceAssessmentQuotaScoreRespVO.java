package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工绩效指标评分明细 Response VO")
@Data
public class HrmPerformanceAssessmentQuotaScoreRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "员工评分阶段编号")
    private Long assessmentStageId;

    @Schema(description = "员工绩效指标编号")
    private Long assessmentQuotaId;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "说明")
    private String comment;

}
