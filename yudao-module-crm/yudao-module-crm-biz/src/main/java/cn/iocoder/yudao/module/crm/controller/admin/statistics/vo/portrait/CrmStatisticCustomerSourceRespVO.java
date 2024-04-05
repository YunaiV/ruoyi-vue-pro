package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户来源分析 VO")
@Data
public class CrmStatisticCustomerSourceRespVO {

    @Schema(description = "客户来源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer source;

    @Schema(description = "客户个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCount;

    @Schema(description = "成交个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dealCount;

}
