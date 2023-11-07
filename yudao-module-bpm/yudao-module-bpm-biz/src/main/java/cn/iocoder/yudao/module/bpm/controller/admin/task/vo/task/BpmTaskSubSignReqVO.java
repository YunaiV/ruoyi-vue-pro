package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

// TODO @海洋：类名，应该是 delete 哈
@Schema(description = "管理后台 - 减签流程任务的 Request VO")
@Data
public class BpmTaskSubSignReqVO {

    @Schema(description = "被减签的任务 ID")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "加签原因")
    @NotEmpty(message = "加签原因不能为空")
    private String reason;
}
