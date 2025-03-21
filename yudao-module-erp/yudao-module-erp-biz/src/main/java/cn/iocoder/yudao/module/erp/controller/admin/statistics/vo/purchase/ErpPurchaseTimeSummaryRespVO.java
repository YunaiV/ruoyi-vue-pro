package cn.iocoder.yudao.module.erp.controller.admin.statistics.vo.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 采购某个时间段的统计 Response VO")
@Data
public class ErpPurchaseTimeSummaryRespVO {

    @Schema(description = "时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-03")
    private String time;

    @Schema(description = "采购金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private BigDecimal price;

}
