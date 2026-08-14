package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - HRM 绩效计划结果等级统计 Response VO")
@Data
public class HrmPerformancePlanResultLevelCountRespVO {

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "数量")
    private Long count;

}
