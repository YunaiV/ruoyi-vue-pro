package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 公海客户分析(按日期) VO")
@Data
public class CrmStatisticsPoolSummaryByDateRespVO {

    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String time;

    @Schema(description = "进入公海客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerPutCount;

    @Schema(description = "公海领取客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerTakeCount;

}
