package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.bytebuddy.implementation.bind.annotation.Empty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 流程任务的更新负责人的 Request VO")
@Data
public class BpmTaskUpdateAssigneeReqVO {

    @Schema(description = "任务编号", required = true, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "新审批人的用户编号", required = true, example = "2048")
    @NotNull(message = "新审批人的用户编号不能为空")
    private Long assigneeUserId;

}
