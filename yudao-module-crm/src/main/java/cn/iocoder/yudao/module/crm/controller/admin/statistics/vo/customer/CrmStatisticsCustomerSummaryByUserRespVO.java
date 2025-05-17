package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 客户总量分析(按用户) VO")
@Data
public class CrmStatisticsCustomerSummaryByUserRespVO extends CrmStatisticsCustomerByUserBaseRespVO {

    @Schema(description = "新建客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCreateCount;

    @Schema(description = "成交客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerDealCount;

    @Schema(description = "合同总金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal contractPrice;

    @Schema(description = "回款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal receivablePrice;

}
