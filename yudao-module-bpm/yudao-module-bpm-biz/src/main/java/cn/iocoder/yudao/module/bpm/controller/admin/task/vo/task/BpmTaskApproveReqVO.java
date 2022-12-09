package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(title = "管理后台 - 通过流程任务的 Request VO")
@Data
public class BpmTaskApproveReqVO {

    @Schema(title = "任务编号", required = true, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(title = "审批意见", required = true, example = "不错不错！")
    @NotEmpty(message = "审批意见不能为空")
    private String reason;

}
