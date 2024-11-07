package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.cc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程实例抄送的分页 Item Response VO")
@Data
public class BpmProcessInstanceCopyRespVO {

    @Schema(description = "抄送主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "发起人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
    private Long startUserId;
    @Schema(description = "发起人昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String startUserName;

    @Schema(description = "流程实例编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "A233")
    private String processInstanceId;
    @Schema(description = "流程实例的名称")
    private String processInstanceName;
    @Schema(description = "流程实例的发起时间")
    private LocalDateTime processInstanceStartTime;

    @Schema(description = "抄送的节点的活动编号")
    private String activityId;
    @Schema(description = "发起抄送的任务编号")
    private String taskId;
    @Schema(description = "发起抄送的任务名称")
    private String taskName;

    @Schema(description = "抄送人")
    private String creator;
    @Schema(description = "抄送人昵称")
    private String creatorName;

    @Schema(description = "抄送时间")
    private LocalDateTime createTime;

}
