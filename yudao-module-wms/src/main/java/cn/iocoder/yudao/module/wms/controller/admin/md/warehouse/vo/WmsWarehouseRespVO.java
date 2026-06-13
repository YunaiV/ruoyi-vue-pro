package cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "仓库编号", example = "WH001")
    @ExcelProperty("仓库编号")
    private String code;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料仓")
    @ExcelProperty("仓库名称")
    private String name;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
