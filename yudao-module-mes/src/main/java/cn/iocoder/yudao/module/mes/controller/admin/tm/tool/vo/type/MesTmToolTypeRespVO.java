package cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工具类型 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesTmToolTypeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "TT-001")
    @ExcelProperty("类型编码")
    private String code;

    @Schema(description = "类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "铣刀")
    @ExcelProperty("类型名称")
    private String name;

    @Schema(description = "是否编码管理", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty("是否编码管理")
    private Boolean codeFlag;

    @Schema(description = "保养维护类型", example = "1")
    @ExcelProperty(value = "保养维护类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_TM_MAINTEN_TYPE)
    private Integer maintenType;

    @Schema(description = "保养周期", example = "30")
    @ExcelProperty("保养周期")
    private Integer maintenPeriod;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
