package cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 计划班组关联新增 Request VO")
@Data
public class MesCalPlanTeamSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "排班计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排班计划不能为空")
    private Long planId;

    @Schema(description = "班组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班组不能为空")
    private Long teamId;

    @Schema(description = "备注")
    private String remark;

    }
