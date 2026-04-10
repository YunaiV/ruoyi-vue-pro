package cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 条码清单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesWmBarcodeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "条码配置编号", example = "1")
    private Long configId;

    @Schema(description = "条码格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("条码格式")
    private Integer format;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    @ExcelProperty("业务类型")
    private Integer bizType;

    @Schema(description = "条码内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH-WH001")
    @ExcelProperty("条码内容")
    private String content;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long bizId;

    @Schema(description = "业务编码", example = "WH001")
    @ExcelProperty("业务编码")
    private String bizCode;

    @Schema(description = "业务名称", example = "原料仓")
    @ExcelProperty("业务名称")
    private String bizName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
