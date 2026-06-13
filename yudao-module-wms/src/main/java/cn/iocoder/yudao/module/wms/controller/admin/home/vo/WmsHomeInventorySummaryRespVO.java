package cn.iocoder.yudao.module.wms.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - WMS 首页库存汇总统计 Response VO")
@Data
public class WmsHomeInventorySummaryRespVO {

    @Schema(description = "总库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    private BigDecimal totalQuantity;

    @Schema(description = "商品库存占比列表")
    private List<ItemRank> goodsShareList;

    @Schema(description = "仓库库存分布列表")
    private List<WarehouseRank> warehouseDistributionList;

    @Schema(description = "管理后台 - WMS 首页商品库存排行 Response VO")
    @Data
    public static class ItemRank {

        @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A4 复印纸")
        private String name;

        @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal quantity;

    }

    @Schema(description = "管理后台 - WMS 首页仓库库存排行 Response VO")
    @Data
    public static class WarehouseRank {

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "上海仓")
        private String name;

        @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal quantity;

    }

}
