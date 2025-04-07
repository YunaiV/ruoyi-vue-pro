package cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 库存盘点结果详情新增/修改 Request VO")
@Data
public class WmsInventoryResultItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1580")
    private Long id;

    @Schema(description = "盘点结果单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2645")
    @NotNull(message = "盘点结果单ID不能为空")
    private Long resultId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20359")
    @NotEmpty(message = "产品ID不能为空")
    private String productId;

    @Schema(description = "预期库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预期库存不能为空")
    private Integer expectedQty;

    @Schema(description = "实际库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实际库存不能为空")
    private Integer actualQty;

}