package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : bin_move_id,product_id,qty,id,from_bin_id,to_bin_id
 */
@Schema(description = "管理后台 - 库位移动详情新增/修改 Request VO")
@Data
public class WmsStockBinMoveItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26371")
    private Long id;

    @Schema(description = "库位移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13416")
    @NotNull(message = "库位移动表ID不能为空")
    private Long binMoveId;

    @Schema(description = "产品ID", example = "4832")
    private Long productId;

    @Schema(description = "调出库位ID", example = "1149")
    private Long fromBinId;

    @Schema(description = "调入库位ID", example = "28214")
    private Long toBinId;

    @Schema(description = "移动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "移动数量不能为空")
    private Integer qty;
}
