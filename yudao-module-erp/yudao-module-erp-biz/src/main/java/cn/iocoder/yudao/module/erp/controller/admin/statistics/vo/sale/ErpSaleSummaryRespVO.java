package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 销售全局统计 Response VO")
@Data
public class ErpSaleSummaryRespVO {

    @Schema(description = "今日销售金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private BigDecimal todayPrice;

    @Schema(description = "昨日销售金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
    private BigDecimal yesterdayPrice;

    @Schema(description = "本月销售金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private BigDecimal monthPrice;

    @Schema(description = "今年销售金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "88888")
    private BigDecimal yearPrice;

}
