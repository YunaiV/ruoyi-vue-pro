package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;


@Schema(description = "管理后台 - CRM 员工业绩统计 Response VO")
@Data
public class CrmStatisticsPerformanceRespVO {

    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String time;

    @Schema(description = "当月统计结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private BigDecimal currentMonthCount;

    @Schema(description = "上月统计结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private BigDecimal lastMonthCount;

    @Schema(description = "去年同期统计结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private BigDecimal lastYearCount;

}
