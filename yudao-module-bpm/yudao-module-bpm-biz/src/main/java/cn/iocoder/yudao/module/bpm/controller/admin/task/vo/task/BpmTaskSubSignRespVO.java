package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 减签流程任务的 Response VO")
@Data
public class BpmTaskSubSignRespVO {
    @Schema(description = "审核的用户信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "小李")
    private BpmTaskRespVO.User assigneeUser;
    @Schema(description = "任务 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12312")
    private String id;
    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "经理审批")
    private String name;
}
