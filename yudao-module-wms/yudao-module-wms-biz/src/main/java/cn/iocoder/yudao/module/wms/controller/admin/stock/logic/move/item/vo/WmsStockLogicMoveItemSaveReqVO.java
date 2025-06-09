package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author jisencai
 * @table-fields : logic_move_id,from_dept_id,product_id,qty,from_company_id,remark,id,to_company_id,to_dept_id
 */
@Schema(description = "管理后台 - 逻辑库存移动详情新增/修改 Request VO")
@Data
public class WmsStockLogicMoveItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20590")
    private Long id;

    @Schema(description = "逻辑移动表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3465")
    @NotNull(message = "逻辑移动表ID不能为空")
    private Long logicMoveId;

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

    @Schema(description = "备注", example = "")
    private String remark;
}
