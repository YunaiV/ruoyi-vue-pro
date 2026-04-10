package cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 编码规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdAutoCodeRuleRespVO {

    @Schema(description = "规则 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("规则 ID")
    private Long id;

    @Schema(description = "规则编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ITEM_CODE")
    @ExcelProperty("规则编码")
    private String code;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "物料编码规则")
    @ExcelProperty("规则名称")
    private String name;

    @Schema(description = "描述", example = "用于生成物料编码")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "最大长度", example = "20")
    @ExcelProperty("最大长度")
    private Integer maxLength;

    @Schema(description = "是否补齐", example = "true")
    @ExcelProperty("是否补齐")
    private Boolean padded;

    @Schema(description = "补齐字符", example = "0")
    @ExcelProperty("补齐字符")
    private String paddedChar;

    @Schema(description = "补齐方式", example = "1")
    @ExcelProperty(value = "补齐方式", converter = DictConvert.class)
    @DictFormat("mes_auto_code_padded_method")
    private Integer paddedMethod;

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
