package cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : inbound_id,quantity,bin_id,product_id,id,inbound_item_id,pickup_id
 */
@Schema(description = "管理后台 - 拣货单详情新增/修改 Request VO")
@Data
public class WmsPickupItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11576")
    private Long id;

    @Schema(description = "拣货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22404")
    @NotNull(message = "拣货单ID不能为空")
    private Long pickupId;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20253")
    @NotNull(message = "入库单ID不能为空")
    private Long inboundId;

    @Schema(description = "入库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21563")
    @NotNull(message = "入库单明细ID不能为空")
    private Integer inboundItemId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17566")
    @NotNull(message = "产品ID不能为空")
    private Integer productId;

    @Schema(description = "拣货数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拣货数量不能为空")
    private Integer quantity;

    @Schema(description = "仓位ID，拣货到目标仓位", requiredMode = Schema.RequiredMode.REQUIRED, example = "8732")
    @NotNull(message = "仓位ID不能为空")
    private Long binId;
}
