package cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 计划班组关联 Response VO")
@Data
public class MesCalPlanTeamRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "排班计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long planId;

    @Schema(description = "班组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long teamId;

    @Schema(description = "班组编码", example = "T001")
    private String teamCode;

    @Schema(description = "班组名称", example = "A组")
    private String teamName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
