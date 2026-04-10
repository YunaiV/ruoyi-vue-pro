package cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 缺陷类型 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesQcDefectRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "缺陷编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "DF001")
    @ExcelProperty("缺陷编码")
    private String code;

    @Schema(description = "缺陷描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "外观缺陷")
    @ExcelProperty("缺陷描述")
    private String name;

    @Schema(description = "检测项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "APPEARANCE")
    @ExcelProperty(value = "检测项类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DEFECT_TYPE)
    private String type;

    @Schema(description = "缺陷等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "缺陷等级", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DEFECT_LEVEL)
    private Integer level;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
