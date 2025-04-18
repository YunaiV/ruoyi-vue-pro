package cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : actual_qty,expected_qty,inventory_id,product_id,remark,id
 */
@Schema(description = "管理后台 - 库存盘点产品新增/修改 Request VO")
@Data
public class WmsInventoryProductSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25474")
    private Long id;

    @Schema(description = "盘点结果单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7233")
    @NotNull(message = "盘点结果单ID不能为空")
    private Long inventoryId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25033")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "预期库存，产品总可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预期库存不能为空")
    private Integer expectedQty;

    @Schema(description = "实际库存，实盘数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实际库存不能为空")
    private Integer actualQty;

    @Schema(description = "备注", example = "")
    private String remark;
}
