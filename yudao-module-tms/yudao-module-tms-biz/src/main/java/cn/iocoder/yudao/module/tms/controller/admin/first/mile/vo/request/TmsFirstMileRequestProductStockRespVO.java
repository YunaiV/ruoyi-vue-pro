package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 产品库存 Response VO")
@Data
public class TmsFirstMileRequestProductStockRespVO {

    @Schema(description = "产品库存列表")
    private List<ProductStock> productStocks;

    @Schema(description = "产品库存")
    @Data
    public static class ProductStock {

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long productId;

        @Schema(description = "可用库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer availableQty;

    }

} 