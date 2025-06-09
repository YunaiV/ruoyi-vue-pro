package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 调拨单可售库存查询 Request VO")
@Data
public class TmsTransferSellableQtyReqVO {

    @Schema(description = "仓库列表")
    private List<WarehouseProduct> warehouses;

    @Data
    public static class WarehouseProduct {
        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long warehouseId;

        @Schema(description = "产品编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> productIds;
    }
} 