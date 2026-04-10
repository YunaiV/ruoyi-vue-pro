package cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 设备保养记录明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvMaintenRecordLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "保养记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long recordId;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long subjectId;

    @Schema(description = "项目名称", example = "检查机油")
    @ExcelProperty("项目名称")
    private String subjectName;

    @Schema(description = "项目内容", example = "检查机油是否充足")
    @ExcelProperty("项目内容")
    private String subjectContent;

    @Schema(description = "项目标准", example = "无漏油")
    @ExcelProperty("标准")
    private String subjectStandard;

    @Schema(description = "保养结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "保养结果", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_MAINTEN_STATUS)
    private Integer status;

    @Schema(description = "异常描述", example = "发现损坏")
    @ExcelProperty("异常描述")
    private String result;

    @Schema(description = "备注", example = "测试备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
