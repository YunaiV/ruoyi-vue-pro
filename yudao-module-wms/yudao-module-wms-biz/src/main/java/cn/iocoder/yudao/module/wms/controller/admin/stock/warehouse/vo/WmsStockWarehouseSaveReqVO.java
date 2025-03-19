package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : available_quantity,defective_quantity,outbound_pending_quantity,purchase_transit_quantity,product_id,shelving_pending_quantity,sellable_quantity,id,return_transit_quantity,purchase_plan_quantity,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存新增/修改 Request VO")
@Data
public class WmsStockWarehouseSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21341")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14491")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3153")
    @NotEmpty(message = "产品ID不能为空")
    private String productId;

    @Schema(description = "产品SKU", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品SKU不能为空")
    private String productSku;

    @Schema(description = "采购计划量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购计划量不能为空")
    private Integer purchasePlanQuantity;

    @Schema(description = "采购在途量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购在途量不能为空")
    private Integer purchaseTransitQuantity;

    @Schema(description = "退件在途数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退件在途数量不能为空")
    private Integer returnTransitQuantity;

    @Schema(description = "待上架数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "待上架数量不能为空")
    private Integer pendingShelvingQuantity;

    @Schema(description = "可用量，在库的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用量不能为空")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可售量不能为空")
    private Integer sellableQuantity;

    @Schema(description = "待出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "待出库量不能为空")
    private Integer pendingOutboundQuantity;

    @Schema(description = "不良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不良品数量不能为空")
    private Integer defectiveQuantity;

    @Schema(description = "待上架数量", example = "")
    private Integer shelvingPendingQuantity;

    @Schema(description = "待出库量", example = "")
    private Integer outboundPendingQuantity;
}
