package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : company_id,outbound_pending_qty,product_id,shelving_pending_qty,available_qty,id,dept_id,warehouse_id
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

    @Schema(description = "库存财务主体公司ID", example = "")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    private Long deptId;

    @Schema(description = "可用库存", example = "")
    private Integer availableQty;

    @Schema(description = "待出库库存", example = "")
    private Integer outboundPendingQty;

    @Schema(description = "待上架数量，上架是指从拣货区上架到货架", example = "")
    private Integer shelvingPendingQty;
}
