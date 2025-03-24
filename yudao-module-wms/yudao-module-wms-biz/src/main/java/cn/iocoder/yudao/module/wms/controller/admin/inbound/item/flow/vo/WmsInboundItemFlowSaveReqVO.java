package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : inbound_id,product_id,id,inbound_item_id,outbound_id,outbound_quantity,outbound_item_id
 */
@Schema(description = "管理后台 - 入库单库存详情扣减新增/修改 Request VO")
@Data
public class WmsInboundItemFlowSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13478")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23778")
    @NotNull(message = "入库单ID不能为空")
    private Long inboundId;

    @Schema(description = "入库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25263")
    @NotNull(message = "入库单明细ID不能为空")
    private Long inboundItemId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30952")
    @NotNull(message = "标准产品ID不能为空")
    private Long productId;

    @Schema(description = "出库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11015")
    @NotNull(message = "出库单ID不能为空")
    private Long outboundId;

    @Schema(description = "出库单明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28163")
    @NotNull(message = "出库单明细ID不能为空")
    private Long outboundItemId;

    @Schema(description = "变化的数量，出库量", example = "")
    private Integer outboundQuantity;
}
