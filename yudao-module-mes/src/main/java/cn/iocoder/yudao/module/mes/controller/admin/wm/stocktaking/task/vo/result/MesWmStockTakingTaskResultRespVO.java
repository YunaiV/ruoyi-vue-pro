package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 盘点结果 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmStockTakingTaskResultRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "任务 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "任务行 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long lineId;

    @Schema(description = "库存台账 ID", example = "1")
    private Long materialStockId;

    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "ITEM001")
    @ExcelProperty("物料编码")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    @ExcelProperty("物料名称")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    @ExcelProperty("规格型号")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    @ExcelProperty("计量单位")
    private String unitMeasureName;

    @Schema(description = "批次 ID", example = "1")
    private Long batchId;

    @Schema(description = "批次编码", example = "B202603080001")
    @ExcelProperty("批次编码")
    private String batchCode;

    @Schema(description = "仓库 ID", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原料仓")
    @ExcelProperty("仓库")
    private String warehouseName;

    @Schema(description = "库区 ID", example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "A 区")
    @ExcelProperty("库区")
    private String locationName;

    @Schema(description = "库位 ID", example = "1")
    private Long areaId;

    @Schema(description = "库位名称", example = "A-01")
    @ExcelProperty("库位")
    private String areaName;

    @Schema(description = "账面数量", example = "100.000000")
    @ExcelProperty("账面数量")
    private BigDecimal quantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

}
