package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.cc;

import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程实例抄送的分页 Item Response VO")
@Data
public class BpmProcessInstanceCopyRespVO {

    @Schema(description = "抄送主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "发起人", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserSimpleBaseVO startUser;

    @Schema(description = "流程实例编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "A233")
    private String processInstanceId;
    @Schema(description = "流程实例的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试")
    private String processInstanceName;
    @Schema(description = "流程实例的发起时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime processInstanceStartTime;

    @Schema(description = "流程活动的编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityId;
    @Schema(description = "流程活动的名字", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityName;

    @Schema(description = "流程活动的编号")
    private String taskId;

    @Schema(description = "抄送人意见")
    private String reason;

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserSimpleBaseVO createUser;

    @Schema(description = "抄送时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
