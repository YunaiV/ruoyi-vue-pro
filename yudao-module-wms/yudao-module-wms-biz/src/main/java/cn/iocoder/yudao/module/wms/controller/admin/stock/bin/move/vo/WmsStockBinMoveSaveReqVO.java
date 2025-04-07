package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.util.List;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;

/**
 * @table-fields : no,id,warehouse_id
 */
@Schema(description = "管理后台 - 库位移动新增/修改 Request VO")
@Data
public class WmsStockBinMoveSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29002")
    private Long id;

    @Schema(description = "单据号")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15798")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "详情清单", example = "")
    private List<WmsStockBinMoveItemSaveReqVO> itemList;
}
