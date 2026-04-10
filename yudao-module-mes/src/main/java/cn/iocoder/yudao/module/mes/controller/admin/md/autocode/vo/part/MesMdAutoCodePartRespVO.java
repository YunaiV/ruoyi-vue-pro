package cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 编码规则组成 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdAutoCodePartRespVO {

    @Schema(description = "分段 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("分段 ID")
    private Long id;

    @Schema(description = "规则 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("规则 ID")
    private Long ruleId;

    @Schema(description = "分段序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("分段序号")
    private Integer sort;

    @Schema(description = "分段类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "分段类型", converter = DictConvert.class)
    @DictFormat("mes_auto_code_part_type")
    private Integer type;

    @Schema(description = "分段长度", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @ExcelProperty("分段长度")
    private Integer length;

    @Schema(description = "日期格式", example = "yyyyMMdd")
    @ExcelProperty("日期格式")
    private String dateFormat;

    @Schema(description = "固定字符", example = "ITEM_")
    @ExcelProperty("固定字符")
    private String fixCharacter;

    @Schema(description = "流水号起始值", example = "1")
    @ExcelProperty("流水号起始值")
    private Integer serialStartNo;

    @Schema(description = "流水号步长", example = "1")
    @ExcelProperty("流水号步长")
    private Integer serialStep;

    @Schema(description = "流水号是否循环", example = "true")
    @ExcelProperty("流水号是否循环")
    private Boolean cycleFlag;

    @Schema(description = "循环方式", example = "3")
    @ExcelProperty(value = "循环方式", converter = DictConvert.class)
    @DictFormat("mes_auto_code_cycle_method")
    private Integer cycleMethod;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
