package cn.iocoder.yudao.module.mes.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 首页生产趋势 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesHomeProductionTrendRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04-05")
    private String date;

    @Schema(description = "产量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    private BigDecimal quantity;

    @Schema(description = "合格品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1200")
    private BigDecimal qualifiedQuantity;

    @Schema(description = "不良品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "34")
    private BigDecimal unqualifiedQuantity;

}
