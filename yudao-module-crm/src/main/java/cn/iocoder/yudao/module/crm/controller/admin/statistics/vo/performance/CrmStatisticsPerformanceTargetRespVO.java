package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 业绩目标完成情况 Response VO")
@Data
@Accessors(chain = true)
public class CrmStatisticsPerformanceTargetRespVO {

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer month;

    @Schema(description = "目标金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000.00")
    private BigDecimal targetPrice;

    @Schema(description = "完成金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "8000.00")
    private BigDecimal currentPrice;

    @Schema(description = "完成率", requiredMode = Schema.RequiredMode.REQUIRED, example = "80.00")
    private BigDecimal completionRate;

}
