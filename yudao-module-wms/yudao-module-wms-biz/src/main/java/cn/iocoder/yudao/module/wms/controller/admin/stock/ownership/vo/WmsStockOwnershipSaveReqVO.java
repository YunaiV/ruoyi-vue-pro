package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : available_quantity,outbound_pending_quantity,company_id,product_id,id,dept_id,warehouse_id
 */
@Schema(description = "管理后台 - 所有者库存新增/修改 Request VO")
@Data
public class WmsStockOwnershipSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17082")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14322")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "1919")
    private Long productId;

    @Schema(description = "产品SKU")
    private String productSku;

    @Schema(description = "库存主体ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28887")
    @NotNull(message = "库存主体ID不能为空")
    private Long inventorySubjectId;

    @Schema(description = "库存归属ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15579")
    @NotNull(message = "库存归属ID不能为空")
    private Long inventoryOwnerId;

    @Schema(description = "可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用库存不能为空")
    private Integer availableQuantity;

    @Schema(description = "待出库库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "待出库库存不能为空")
    private Integer pendingOutboundQuantity;

    @Schema(description = "库存财务主体公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "待出库库存", example = "")
    private Integer outboundPendingQuantity;
}
