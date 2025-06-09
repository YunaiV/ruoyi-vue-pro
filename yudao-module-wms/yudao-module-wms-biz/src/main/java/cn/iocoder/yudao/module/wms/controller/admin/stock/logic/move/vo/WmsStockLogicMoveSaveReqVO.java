package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsMoveExecuteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @table-fields : no,execute_status,id,warehouse_id
 */
@Schema(description = "管理后台 - 逻辑库存移动新增/修改 Request VO")
@Data
public class WmsStockLogicMoveSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2070")
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "库存移动的执行状态 ; WmsMoveExecuteStatus : 0-草稿 , 1-已执行", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(WmsMoveExecuteStatus.class)
    private Integer executeStatus;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13471")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "详情清单", example = "")
    private List<WmsStockLogicMoveItemSaveReqVO> itemList;
}
