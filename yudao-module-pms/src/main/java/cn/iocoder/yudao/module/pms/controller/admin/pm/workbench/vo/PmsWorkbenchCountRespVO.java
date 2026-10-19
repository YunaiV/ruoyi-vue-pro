package cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - PMS 工作台数量响应 VO")
@Data
@Accessors(chain = true)
public class PmsWorkbenchCountRespVO {

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer requirementCount;

    @Schema(description = "任务数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer taskCount;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer defectCount;

    @Schema(description = "迭代数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer iterationCount;

}
