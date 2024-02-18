package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 采购全局统计 Response VO")
@Data
public class ErpPurchaseSummaryRespVO {

    @Schema(description = "今日采购金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private BigDecimal todayPrice;

    @Schema(description = "昨日采购金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
    private BigDecimal yesterdayPrice;

    @Schema(description = "本月采购金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private BigDecimal monthPrice;

    @Schema(description = "今年采购金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "88888")
    private BigDecimal yearPrice;

}
