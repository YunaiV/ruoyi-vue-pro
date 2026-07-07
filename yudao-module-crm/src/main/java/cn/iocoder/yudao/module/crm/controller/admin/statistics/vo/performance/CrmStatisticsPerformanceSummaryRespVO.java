package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 员工业绩合同汇总 Response VO")
@Data
public class CrmStatisticsPerformanceSummaryRespVO {

    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String time;

    @Schema(description = "合同数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer contractCount;

    @Schema(description = "合同金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000.00")
    private BigDecimal contractPrice;

    @Schema(description = "回款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "8000.00")
    private BigDecimal receivablePrice;

    @Schema(description = "未回款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000.00")
    private BigDecimal unreceivedPrice;

}
