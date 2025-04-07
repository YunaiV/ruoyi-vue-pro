package cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 所有者库存移动详情新增/修改 Request VO")
@Data
public class WmsStockOwnershiopMoveItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19084")
    private Long id;

    @Schema(description = "所有者移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16712")
    @NotNull(message = "所有者移动表ID不能为空")
    private Long ownershipMoveId;

    @Schema(description = "产品ID", example = "11262")
    private Integer productId;

    @Schema(description = "调出财务公司ID", example = "6416")
    private Long fromCompanyId;

    @Schema(description = "调出部门ID", example = "11905")
    private Long fromDeptId;

    @Schema(description = "调入财务公司ID", example = "29642")
    private Long toCompanyId;

    @Schema(description = "调入部门ID", example = "17818")
    private Long toDeptId;

    @Schema(description = "移动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "移动数量不能为空")
    private Integer qty;

}