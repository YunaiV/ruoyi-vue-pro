package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 不通过流程任务的 Request VO")
@Data
public class BpmTaskRejectReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "审批意见", requiredMode = Schema.RequiredMode.REQUIRED, example = "不错不错！")
    @NotEmpty(message = "审批意见不能为空")
    private String reason;

}
