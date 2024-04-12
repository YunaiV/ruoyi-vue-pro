package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 成交周期分析(按用户) VO")
@Data
public class CrmStatisticsCustomerDealCycleByUserRespVO extends CrmStatisticsCustomerByUserBaseRespVO {

    @Schema(description = "成交周期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    private Double customerDealCycle;

    @Schema(description = "成交客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerDealCount;

}
