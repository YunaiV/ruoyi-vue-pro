package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 流程任务的精简 Response VO")
@Data
public class BpmTaskSimpleRespVO {

    @Schema(description = "任务定义的标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "Activity_one")
    private String definitionKey;

    @Schema(description = "任务名词", requiredMode = Schema.RequiredMode.REQUIRED, example = "经理审批")
    private String name;

}
