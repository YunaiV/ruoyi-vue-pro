package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户成交周期分析(按区域) VO")
@Data
public class CrmStatisticsCustomerDealCycleByAreaRespVO {

    @Schema(description = "省份编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonIgnore
    private Integer areaId;

    @Schema(description = "省份名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "浙江省")
    private String areaName;

    @Schema(description = "成交周期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    private Double customerDealCycle;

    @Schema(description = "成交客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerDealCount;

}
