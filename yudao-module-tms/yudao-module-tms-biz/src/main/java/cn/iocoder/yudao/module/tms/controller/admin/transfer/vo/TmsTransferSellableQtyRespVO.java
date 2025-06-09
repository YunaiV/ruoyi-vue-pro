package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 调拨单可售库存查询 Response VO")
@Data
public class TmsTransferSellableQtyRespVO {

    @Schema(description = "仓库编号 -> 产品可售库存列表")
    private Map<Long, List<ProductSellableQty>> warehouseProductMap;

    @Data
    public static class ProductSellableQty {
        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;

        @Schema(description = "可售数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer sellableQty;
    }
} 