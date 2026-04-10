package cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 排班计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesCalPlanRespVO {

    @Schema(description = "计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("计划编号")
    private Long id;

    @Schema(description = "计划编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PLAN001")
    @ExcelProperty("计划编码")
    private String code;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024年排班计划")
    @ExcelProperty("计划名称")
    private String name;

    @Schema(description = "班组类型", example = "1")
    @ExcelProperty("班组类型")
    private Integer calendarType;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开始日期")
    private LocalDateTime startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("结束日期")
    private LocalDateTime endDate;

    @Schema(description = "轮班方式", example = "1")
    @ExcelProperty("轮班方式")
    private Integer shiftType;

    @Schema(description = "倒班方式", example = "1")
    @ExcelProperty("倒班方式")
    private Integer shiftMethod;

    @Schema(description = "倒班天数", example = "7")
    @ExcelProperty("倒班天数")
    private Integer shiftCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
