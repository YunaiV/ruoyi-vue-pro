package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 出库单详情新增/修改 Request VO")
@Data
public class WmsOutboundItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27153")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6602")
    @NotNull(message = "入库单ID不能为空")
    private Long outboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20572")
    @NotNull(message = "标准产品ID不能为空")
    private Long productId;

    @Schema(description = "标准产品SKU", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标准产品SKU不能为空")
    private String productSku;

    @Schema(description = "预期量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预期量不能为空")
    private Integer expectedQuantity;

    @Schema(description = "实际量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实际量不能为空")
    private Integer actualQuantity;

    @Schema(description = "来源详情ID", example = "11448")
    private Long sourceItemId;

}