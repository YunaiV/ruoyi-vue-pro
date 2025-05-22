package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 跟进次数分析(按类型) VO")
@Data
public class CrmStatisticsFollowUpSummaryByTypeRespVO {

    @Schema(description = "跟进类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer followUpType;

    @Schema(description = "跟进次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer followUpRecordCount;

}
