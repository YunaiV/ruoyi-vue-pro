package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.location;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 库区 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmWarehouseLocationRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "库区编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "L001")
    @ExcelProperty("库区编码")
    private String code;

    @Schema(description = "库区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料区")
    @ExcelProperty("库区名称")
    private String name;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原料仓")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    @Schema(description = "面积", example = "500.00")
    @ExcelProperty("面积")
    private BigDecimal area;

    @Schema(description = "是否冻结", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty("是否冻结")
    private Boolean frozen;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
