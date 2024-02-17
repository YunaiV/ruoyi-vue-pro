package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - 流程实例抄送的创建 Request VO")
@Data
public class BpmProcessInstanceCopyCreateReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String taskId;

    @Schema(description = "抄送原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "请帮忙审查下！")
    @NotBlank(message = "抄送原因不能为空")
    private String reason;

}
