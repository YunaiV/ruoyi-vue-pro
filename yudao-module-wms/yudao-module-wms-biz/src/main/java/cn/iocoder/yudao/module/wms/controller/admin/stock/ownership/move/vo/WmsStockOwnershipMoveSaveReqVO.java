package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemSaveReqVO;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.stock.WmsMoveExecuteStatus;

/**
 * @table-fields : no,execute_status,id,warehouse_id
 */
@Schema(description = "管理后台 - 所有者库存移动新增/修改 Request VO")
@Data
public class WmsStockOwnershipMoveSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2070")
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "执行状态不能为空")
    @InEnum(WmsMoveExecuteStatus.class)
    private Integer executeStatus;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13471")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "详情清单", example = "")
    private List<WmsStockOwnershipMoveItemSaveReqVO> itemList;
}
