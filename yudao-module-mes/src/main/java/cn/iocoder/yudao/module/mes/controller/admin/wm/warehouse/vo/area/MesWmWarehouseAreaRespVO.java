package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 库位 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmWarehouseAreaRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "库位编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A001")
    @ExcelProperty("库位编码")
    private String code;

    @Schema(description = "库位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认库位")
    @ExcelProperty("库位名称")
    private String name;

    @Schema(description = "库区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "原料区")
    @ExcelProperty("库区名称")
    private String locationName;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原料仓")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    @Schema(description = "面积", example = "20.00")
    @ExcelProperty("面积")
    private BigDecimal area;

    @Schema(description = "最大载重", example = "1000.00")
    @ExcelProperty("最大载重")
    private BigDecimal maxLoad;

    @Schema(description = "位置 X", example = "1")
    @ExcelProperty("位置 X")
    private Integer positionX;

    @Schema(description = "位置 Y", example = "1")
    @ExcelProperty("位置 Y")
    private Integer positionY;

    @Schema(description = "位置 Z", example = "1")
    @ExcelProperty("位置 Z")
    private Integer positionZ;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "是否冻结", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否冻结")
    private Boolean frozen;

    @Schema(description = "是否允许物料混放", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty("允许物料混放")
    private Boolean allowItemMixing;

    @Schema(description = "是否允许批次混放", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty("允许批次混放")
    private Boolean allowBatchMixing;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
