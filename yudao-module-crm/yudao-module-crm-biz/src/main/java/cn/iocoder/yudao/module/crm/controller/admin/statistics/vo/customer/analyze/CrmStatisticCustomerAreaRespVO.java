package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户省份分析 VO")
@Data
public class CrmStatisticCustomerAreaRespVO {

    @Schema(description = "省份编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer areaId;
    @Schema(description = "省份名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "浙江省")
    private String areaName;

    @Schema(description = "客户个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCount;

    @Schema(description = "成交个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dealCount;

    @Schema(description = "省份占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double areaPortion;

    @Schema(description = "成交占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double dealPortion;

}
