package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户总量分析(按日期) VO")
@Data
public class CrmStatisticsCustomerSummaryByDateRespVO {

    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String time;

    @Schema(description = "新建客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCreateCount;

    @Schema(description = "成交客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerDealCount;

}
