package cn.iocoder.yudao.module.statistics.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @疯狂：是不是搞成公用的哈；类似，controller/admin/common，下面直接放它
@Schema(description = "管理后台 - 交易统计对照 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeStatisticsComparisonRespVO<T> {

    @Schema(description = "当前数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private T value;

    @Schema(description = "参照数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private T reference;

}
