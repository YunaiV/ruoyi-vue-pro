package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 流程任务分配规则的 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskAssignRuleRespVO extends BpmTaskAssignRuleBaseVO {

    @Schema(description = "任务分配规则的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "流程模型的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private String modelId;

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    private String processDefinitionId;

    @Schema(description = "流程任务定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private String taskDefinitionKey;
    @Schema(description = "流程任务定义的名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "关注芋道")
    private String taskDefinitionName;

}
