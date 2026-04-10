package cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 设备点检记录明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvCheckRecordLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "点检记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long recordId;

    @Schema(description = "点检项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long subjectId;

    @Schema(description = "项目编码", example = "S001")
    @ExcelProperty("项目编码")
    private String subjectCode;

    @Schema(description = "项目名称", example = "润滑油检查")
    @ExcelProperty("项目名称")
    private String subjectName;

    @Schema(description = "项目类型", example = "1")
    @ExcelProperty("项目类型")
    private Integer subjectType;

    @Schema(description = "检查内容", example = "检查润滑油是否充足")
    @ExcelProperty("检查内容")
    private String subjectContent;

    @Schema(description = "检查标准", example = "油位不低于最低刻度")
    @ExcelProperty("检查标准")
    private String subjectStandard;

    @Schema(description = "点检结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "点检结果", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_CHECK_RESULT)
    private Integer checkStatus;

    @Schema(description = "异常描述", example = "设备异响")
    @ExcelProperty("异常描述")
    private String checkResult;

    @Schema(description = "备注", example = "测试备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
