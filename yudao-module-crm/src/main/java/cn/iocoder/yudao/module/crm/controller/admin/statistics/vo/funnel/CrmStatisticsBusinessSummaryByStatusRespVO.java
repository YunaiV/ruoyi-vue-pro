package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 商机阶段统计 Response VO")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CrmStatisticsBusinessSummaryByStatusRespVO {

    @Schema(description = "商机阶段编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long statusId;

    @Schema(description = "商机阶段名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "初步接洽")
    private String statusName;

    @Schema(description = "赢单率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer statusPercent;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "商机数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long businessCount;

    @Schema(description = "商机总金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private BigDecimal totalPrice;

}
