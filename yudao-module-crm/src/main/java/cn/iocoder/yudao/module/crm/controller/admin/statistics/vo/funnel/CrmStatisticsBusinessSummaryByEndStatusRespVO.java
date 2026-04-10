package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 商机结束状态统计 Response VO")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CrmStatisticsBusinessSummaryByEndStatusRespVO {

    @Schema(description = "结束状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer endStatus;

    @Schema(description = "商机数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long businessCount;

    @Schema(description = "商机总金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private BigDecimal totalPrice;

}
