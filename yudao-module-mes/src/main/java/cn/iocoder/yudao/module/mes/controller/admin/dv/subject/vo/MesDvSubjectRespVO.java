package cn.iocoder.yudao.module.mes.controller.admin.dv.subject.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 点检保养项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvSubjectRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "项目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "CHK001")
    @ExcelProperty("项目编码")
    private String code;

    @Schema(description = "项目名称", example = "注塑机外观检查")
    @ExcelProperty("项目名称")
    private String name;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "项目类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_SUBJECT_TYPE)
    private Integer type;

    @Schema(description = "项目内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "检查注塑机外壳是否有裂纹")
    @ExcelProperty("项目内容")
    private String content;

    @Schema(description = "标准", example = "外观完好，无明显损伤")
    @ExcelProperty("标准")
    private String standard;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
