package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 新增商机分析(按日期) VO")
@Data
public class CrmStatisticsBusinessSummaryByDateRespVO {

    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String time;

    @Schema(description = "新增商机数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long businessCreateCount;

    @Schema(description = "新增商机金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private BigDecimal totalPrice;

}
