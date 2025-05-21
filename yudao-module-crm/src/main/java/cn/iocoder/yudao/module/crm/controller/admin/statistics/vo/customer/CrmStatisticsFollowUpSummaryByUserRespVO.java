package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 跟进次数分析(按用户) VO")
@Data
public class CrmStatisticsFollowUpSummaryByUserRespVO extends CrmStatisticsCustomerByUserBaseRespVO {

    @Schema(description = "跟进次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer followUpRecordCount;

    @Schema(description = "跟进客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer followUpCustomerCount;

}
