package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 流程实例的取消 Request VO")
@Data
public class BpmProcessInstanceCancelReqVO {

    @Schema(description = "流程实例的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "流程实例的编号不能为空")
    private String id;

    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不请假了！")
    @NotEmpty(message = "取消原因不能为空")
    private String reason;

}
