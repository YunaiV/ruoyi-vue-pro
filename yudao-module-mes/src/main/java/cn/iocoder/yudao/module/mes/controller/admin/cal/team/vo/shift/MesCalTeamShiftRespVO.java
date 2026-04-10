package cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 班组排班 Response VO")
@Data
public class MesCalTeamShiftRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "排班计划编号", example = "1")
    private Long planId;

    @Schema(description = "班组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    private Long teamId;

    @Schema(description = "班组名称", example = "注塑A组")
    private String teamName;

    @Schema(description = "班次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long shiftId;

    @Schema(description = "班次名称", example = "白班")
    private String shiftName;

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime day;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
