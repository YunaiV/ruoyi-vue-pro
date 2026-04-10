package cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 维修工单行 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvRepairLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "维修工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long repairId;

    @Schema(description = "点检保养项目编号", example = "1")
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

    @Schema(description = "故障描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "液压系统漏油")
    @ExcelProperty("故障描述")
    private String malfunction;

    @Schema(description = "故障图片 URL", example = "https://example.com/image.png")
    private String malfunctionUrl;

    @Schema(description = "维修描述", example = "更换密封圈")
    @ExcelProperty("维修描述")
    private String description;

    @Schema(description = "备注", example = "测试备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
