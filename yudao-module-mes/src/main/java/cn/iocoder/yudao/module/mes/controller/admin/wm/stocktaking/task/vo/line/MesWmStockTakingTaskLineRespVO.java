package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 盘点任务行 Response VO")
@Data
public class MesWmStockTakingTaskLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "任务 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "库存台账 ID", example = "1")
    private Long materialStockId;

    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "批次 ID", example = "1")
    private Long batchId;

    @Schema(description = "批次编码", example = "B202603080001")
    private String batchCode;

    @Schema(description = "账面数量", example = "100.000000")
    private BigDecimal quantity;

    @Schema(description = "实盘数量", example = "98.000000")
    private BigDecimal takingQuantity;

    @Schema(description = "差异数量", example = "-2.000000")
    private BigDecimal differenceQuantity;

    @Schema(description = "仓库 ID", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原料仓")
    private String warehouseName;

    @Schema(description = "库区 ID", example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "A 区")
    private String locationName;

    @Schema(description = "库位 ID", example = "1")
    private Long areaId;

    @Schema(description = "库位名称", example = "A-01")
    private String areaName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
