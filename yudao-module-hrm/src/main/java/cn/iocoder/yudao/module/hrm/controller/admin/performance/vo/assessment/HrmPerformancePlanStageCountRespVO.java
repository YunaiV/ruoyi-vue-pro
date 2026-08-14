package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - HRM 绩效计划阶段统计 Response VO")
@Data
public class HrmPerformancePlanStageCountRespVO {

    @Schema(description = "阶段状态")
    private Integer stageType;

    @Schema(description = "数量")
    private Long count;

}
