package cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 质检指标 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesQcIndicatorRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "检测项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "QI001")
    @ExcelProperty("检测项编码")
    private String code;

    @Schema(description = "检测项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "长度")
    @ExcelProperty("检测项名称")
    private String name;

    @Schema(description = "检测项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "SIZE")
    @ExcelProperty(value = "检测项类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_INDICATOR_TYPE)
    private String type;

    @Schema(description = "检测工具", example = "卡尺")
    @ExcelProperty("检测工具")
    private String tool;

    @Schema(description = "结果值类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "结果值类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_QC_RESULT_TYPE)
    private Integer resultType;

    @Schema(description = "结果值属性", example = "IMG")
    @ExcelProperty("结果值属性")
    private String resultSpecification;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
