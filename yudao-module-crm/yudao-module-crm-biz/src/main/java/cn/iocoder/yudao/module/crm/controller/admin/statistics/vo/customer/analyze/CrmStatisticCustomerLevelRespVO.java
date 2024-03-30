package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户级别分析 VO")
@Data
public class CrmStatisticCustomerLevelRespVO {

    @Schema(description = "客户级别ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer level;
    @Schema(description = "客户级别名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String levelName;

    @Schema(description = "客户个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCount;

    @Schema(description = "成交个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dealCount;

    @Schema(description = "级别占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double levelPortion;

    @Schema(description = "成交占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double dealPortion;

}
