package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 盘点任务行新增/修改 Request VO")
@Data
public class MesWmStockTakingTaskLineSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "盘点任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "盘点任务编号不能为空")
    private Long taskId;

    @Schema(description = "库存记录编号", example = "1")
    private Long materialStockId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long itemId;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "账面数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "账面数量不能为空")
    private BigDecimal quantity;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
