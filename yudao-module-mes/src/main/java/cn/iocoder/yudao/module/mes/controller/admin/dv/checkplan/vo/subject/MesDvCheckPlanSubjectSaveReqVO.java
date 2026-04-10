package cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 点检保养方案项目新增 Request VO")
@Data
public class MesDvCheckPlanSubjectSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "方案编号不能为空")
    private Long planId;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "点检保养项目不能为空")
    private Long subjectId;

    @Schema(description = "备注")
    private String remark;

    }
