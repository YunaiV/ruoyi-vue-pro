package cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 班组新增/修改 Request VO")
@Data
public class MesCalTeamSaveReqVO {

    @Schema(description = "班组编号", example = "1024")
    private Long id;

    @Schema(description = "班组编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "TEAM-A")
    @NotEmpty(message = "班组编码不能为空")
    private String code;

    @Schema(description = "班组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "注塑A组")
    @NotEmpty(message = "班组名称不能为空")
    private String name;

    @Schema(description = "班组类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班组类型不能为空")
    private Integer calendarType;

    @Schema(description = "备注")
    private String remark;

}
