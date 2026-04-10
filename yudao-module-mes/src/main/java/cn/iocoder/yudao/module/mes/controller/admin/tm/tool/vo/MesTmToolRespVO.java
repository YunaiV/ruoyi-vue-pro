package cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工具台账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesTmToolRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "工具编码", example = "T-001")
    @ExcelProperty("工具编码")
    private String code;

    @Schema(description = "工具名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "5mm 铣刀")
    @ExcelProperty("工具名称")
    private String name;

    @Schema(description = "品牌", example = "三菱")
    @ExcelProperty("品牌")
    private String brand;

    @Schema(description = "型号规格", example = "M5-100")
    @ExcelProperty("型号规格")
    private String spec;

    @Schema(description = "工具类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long toolTypeId;

    @Schema(description = "工具类型名称", example = "铣刀")
    @ExcelProperty("工具类型")
    private String toolTypeName;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("数量")
    private Integer quantity;

    @Schema(description = "可用数量", example = "1")
    @ExcelProperty("可用数量")
    private Integer availableQuantity;

    @Schema(description = "保养维护类型", example = "1")
    @ExcelProperty(value = "保养维护类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_TM_MAINTEN_TYPE)
    private Integer maintenType;

    @Schema(description = "下次保养周期（次数）", example = "100")
    @ExcelProperty("下次保养周期")
    private Integer nextMaintenPeriod;

    @Schema(description = "下次保养日期", example = "2024-06-01 00:00:00")
    @ExcelProperty("下次保养日期")
    private LocalDateTime nextMaintenDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_TM_TOOL_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
