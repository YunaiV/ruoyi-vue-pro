package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : outbound_pending_qty,product_id,shelving_pending_qty,available_qty,purchase_transit_qty,id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,warehouse_id
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
    private Long productId;

    @Schema(description = "可用量，在库的良品数量", example = "")
    private Integer availableQty;

    @Schema(description = "不良品数量", example = "")
    private Integer defectiveQty;

    @Schema(description = "待出库量", example = "")
    private Integer outboundPendingQty;

    @Schema(description = "采购计划量", example = "")
    private Integer purchasePlanQty;

    @Schema(description = "采购在途量", example = "")
    private Integer purchaseTransitQty;

    @Schema(description = "退件在途数量", example = "")
    private Integer returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    private Integer sellableQty;

    @Schema(description = "待上架数量，上架是指从拣货区上架到货架", example = "")
    private Integer shelvingPendingQty;
}
