package cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 排班计划新增/修改 Request VO")
@Data
public class MesCalPlanSaveReqVO {

    @Schema(description = "计划编号", example = "1024")
    private Long id;

    @Schema(description = "计划编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PLAN001")
    @NotEmpty(message = "计划编码不能为空")
    private String code;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024年排班计划")
    @NotEmpty(message = "计划名称不能为空")
    private String name;

    @Schema(description = "班组类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班组类型不能为空")
    private Integer calendarType;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    private LocalDateTime startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDateTime endDate;

    @Schema(description = "轮班方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "轮班方式不能为空")
    private Integer shiftType;

    @Schema(description = "倒班方式", example = "1")
    private Integer shiftMethod;

    @Schema(description = "倒班天数", example = "7")
    private Integer shiftCount;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    }
