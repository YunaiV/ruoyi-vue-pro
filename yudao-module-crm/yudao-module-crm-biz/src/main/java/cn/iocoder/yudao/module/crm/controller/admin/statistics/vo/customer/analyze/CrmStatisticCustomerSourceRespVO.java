package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户来源分析 VO")
@Data
public class CrmStatisticCustomerSourceRespVO {

    @Schema(description = "客户来源ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer source;
    @Schema(description = "客户来源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String sourceName;

    @Schema(description = "客户个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCount;

    @Schema(description = "成交个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dealCount;

    @Schema(description = "来源占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double sourcePortion;

    @Schema(description = "成交占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double dealPortion;

}
