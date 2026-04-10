package cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 计量单位 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdUnitMeasureRespVO {

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("单位编号")
    private Long id;

    @Schema(description = "单位编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "KG")
    @ExcelProperty("单位编码")
    private String code;

    @Schema(description = "单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "公斤")
    @ExcelProperty("单位名称")
    private String name;

    @Schema(description = "是否主单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty("是否主单位")
    private Boolean primaryFlag;

    @Schema(description = "主单位编号", example = "200")
    @ExcelProperty("主单位编号")
    private Long primaryId;

    @Schema(description = "与主单位换算比例", example = "1000.0000")
    @ExcelProperty("与主单位换算比例")
    private BigDecimal changeRate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
