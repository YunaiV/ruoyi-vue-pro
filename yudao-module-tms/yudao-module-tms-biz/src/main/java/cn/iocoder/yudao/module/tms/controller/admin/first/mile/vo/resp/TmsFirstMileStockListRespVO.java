package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 头程单 - 库存查询列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 头程单库存查询列表 Response VO")
public class TmsFirstMileStockListRespVO {

    @Schema(description = "产品库存列表")
    private List<ProductStockVO> productStocks;

    @Data
    @Schema(description = "产品库存信息")
    public static class ProductStockVO {

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long productId;

        @Schema(description = "库存列表")
        private List<TmsFirstMileStockRespVO> stocks;
    }
} 