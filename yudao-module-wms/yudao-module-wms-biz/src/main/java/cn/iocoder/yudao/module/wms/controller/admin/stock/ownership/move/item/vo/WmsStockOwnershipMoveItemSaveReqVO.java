package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : ownership_move_id,from_dept_id,product_id,qty,from_company_id,id,to_company_id,to_dept_id
 */
@Schema(description = "管理后台 - 所有者库存移动详情新增/修改 Request VO")
@Data
public class WmsStockOwnershipMoveItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20590")
    private Long id;

    @Schema(description = "所有者移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3465")
    @NotNull(message = "所有者移动表ID不能为空")
    private Long ownershipMoveId;

    @Schema(description = "产品ID", example = "2464")
    private Long productId;

    @Schema(description = "调出财务公司ID", example = "28314")
    private Long fromCompanyId;

    @Schema(description = "调出部门ID", example = "7494")
    private Long fromDeptId;

    @Schema(description = "调入财务公司ID", example = "11668")
    private Long toCompanyId;

    @Schema(description = "调入部门ID", example = "12421")
    private Long toDeptId;

    @Schema(description = "移动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "移动数量不能为空")
    private Integer qty;
}
