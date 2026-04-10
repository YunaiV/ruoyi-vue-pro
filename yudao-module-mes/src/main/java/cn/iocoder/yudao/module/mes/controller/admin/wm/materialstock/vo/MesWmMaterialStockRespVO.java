package cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 库存台账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmMaterialStockRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "物料分类编号", example = "1")
    private Long itemTypeId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
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

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "B20260101")
    @ExcelProperty("批次号")
    private String batchCode;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原料仓")
    @ExcelProperty("仓库")
    private String warehouseName;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "A 区")
    @ExcelProperty("库区")
    private String locationName;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "库位名称", example = "A-01")
    @ExcelProperty("库位")
    private String areaName;

    @Schema(description = "供应商编号", example = "1")
    private Long vendorId;

    @Schema(description = "供应商名称", example = "某供应商")
    @ExcelProperty("供应商")
    private String vendorName;

    @Schema(description = "在库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.0000")
    @ExcelProperty("在库数量")
    private BigDecimal quantity;

    @Schema(description = "入库时间")
    @ExcelProperty("入库时间")
    private LocalDateTime receiptTime;

    @Schema(description = "是否冻结", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否冻结")
    private Boolean frozen;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
