package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : inbound_id,product_sku,source_item_id,left_quantity,actual_quantity,product_id,plan_quantity,id
 */
@Schema(description = "管理后台 - 入库单详情新增/修改 Request VO")
@Data
public class WmsInboundItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30520")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29327")
    @NotNull(message = "入库单ID不能为空")
    private Long inboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27659")
    @NotNull(message = "标准产品ID不能为空")
    private Long productId;

    @Schema(description = "标准产品SKU", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标准产品SKU不能为空")
    private String productSku;

    @Schema(description = "计划入库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划入库量不能为空")
    private Integer planQuantity;

    @Schema(description = "实际入库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实际入库量不能为空")
    private Integer actualQuantity;

    @Schema(description = "批次剩余库存，出库后的剩余库存量")
    private Integer leftQuantity;

    @Schema(description = "来源详情ID", example = "30830")
    private Long sourceItemId;
}
