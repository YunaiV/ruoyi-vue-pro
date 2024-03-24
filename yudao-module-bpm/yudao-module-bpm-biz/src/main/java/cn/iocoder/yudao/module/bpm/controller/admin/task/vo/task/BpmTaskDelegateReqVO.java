package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 委派流程任务的 Request VO")
@Data
public class BpmTaskDelegateReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "被委派人 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "被委派人 ID 不能为空")
    private Long delegateUserId;

    @Schema(description = "委派原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "做不了决定，需要你先帮忙瞅瞅")
    @NotEmpty(message = "委派原因不能为空")
    private String reason;

}
