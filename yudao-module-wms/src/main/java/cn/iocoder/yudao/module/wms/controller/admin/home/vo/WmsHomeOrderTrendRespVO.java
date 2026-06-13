package cn.iocoder.yudao.module.wms.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 首页单据趋势 Response VO")
@Data
public class WmsHomeOrderTrendRespVO {

    @Schema(description = "时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1778169600000")
    private LocalDateTime time;

    @Schema(description = "入库单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long receiptCount;

    @Schema(description = "出库单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Long shipmentCount;

    @Schema(description = "移库单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long movementCount;

    @Schema(description = "盘库单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long checkCount;

}
