package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.StringListConvert;
import cn.iocoder.yudao.module.fms.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.infra.enums.DictTypeConstants.BOOLEAN_STRING;

/**
 * FMS 科目 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 科目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsSubjectRespVO {

    @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @ExcelProperty(value = "编码", index = 0)
    private String code;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "库存现金")
    @ExcelProperty(value = "名称", index = 1)
    private String name;

    @Schema(description = "上级科目编号", example = "1024")
    private Long parentId;

    @Schema(description = "科目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "科目类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer category;

    @JsonIgnore // 仅 excel 导出使用
    @ExcelProperty(value = "类别", index = 2, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SUBJECT_CATEGORY)
    private String categoryDictValue;

    @Schema(description = "余额方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "余额方向", index = 3, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DEBIT_CREDIT_DIRECTION)
    private Integer balanceDirection;

    @Schema(description = "辅助核算类别编号数组")
    private List<Long> auxiliaryTypeIds;

    @Schema(description = "外币核算币别编号数组")
    private List<Long> currencyIds;

    @Schema(description = "是否启用数量核算", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean quantityAccounting;

    @Schema(description = "数量单位", example = "件")
    @ExcelProperty(value = "数量", index = 5)
    private String quantityUnit;

    @Schema(description = "是否现金及现金等价物", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "是否现金项", index = 6, converter = DictConvert.class)
    @DictFormat(BOOLEAN_STRING)
    private Boolean cash;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "辅助核算类别名称数组")
    @ExcelProperty(value = "辅助核算", index = 4, converter = StringListConvert.class)
    private List<String> auxiliaryTypeNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
