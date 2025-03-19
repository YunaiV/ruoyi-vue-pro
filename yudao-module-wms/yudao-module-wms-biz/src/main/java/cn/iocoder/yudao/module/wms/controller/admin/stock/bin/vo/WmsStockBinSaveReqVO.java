package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : available_quantity,outbound_pending_quantity,bin_id,product_id,sellable_quantity,id,warehouse_id
 */
@Schema(description = "管理后台 - 仓位库存新增/修改 Request VO")
@Data
public class WmsStockBinSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30764")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "748")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "库位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10839")
    @NotNull(message = "库位ID不能为空")
    private Long binId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11713")
    @NotEmpty(message = "产品ID不能为空")
    private String productId;

    @Schema(description = "可用量，在库的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用量不能为空")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可售量不能为空")
    private Integer sellableQuantity;

    @Schema(description = "待出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "待出库量不能为空")
    private Integer outboundPendingQuantity;
}
