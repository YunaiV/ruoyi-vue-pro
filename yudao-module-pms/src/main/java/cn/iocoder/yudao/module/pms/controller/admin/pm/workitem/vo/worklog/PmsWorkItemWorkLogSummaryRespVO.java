package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项工时汇总 Response VO")
@Data
public class PmsWorkItemWorkLogSummaryRespVO {

    @Schema(description = "预估工时，单位：小时", example = "16")
    private Integer estimatedHours;

    @Schema(description = "已登记工时，单位：小时", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer actualHours;

    @Schema(description = "剩余工时，单位：小时", example = "8")
    private Integer remainingHours;

    @Schema(description = "工时记录列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsWorkItemWorkLogRespVO> records;

}
