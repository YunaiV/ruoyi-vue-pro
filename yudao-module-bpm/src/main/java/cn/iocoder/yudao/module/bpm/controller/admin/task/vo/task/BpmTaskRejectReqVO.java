package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "管理后台 - 不通过流程任务的 Request VO")
@Data
public class BpmTaskRejectReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "审批意见", requiredMode = Schema.RequiredMode.REQUIRED, example = "不错不错！")
    private String reason;

    @Schema(description = "附件", example = "[https://test.yudao.iocoder.cn/20260609/test.txt]")
    private List<String> attachments;

    @Schema(description = "退回修改的批次编号，来自待办任务。任务退回修改过时必须传递", example = "a1b2c3")
    private String revisionId;

}
