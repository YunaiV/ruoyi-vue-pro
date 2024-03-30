package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户成交周期分析(按日期) VO")
@Data
public class CrmStatisticsCustomerDealCycleByDateRespVO {

    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String time;

    @Schema(description = "成交周期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    private Double customerDealCycle;

}
