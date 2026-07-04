package cn.iocoder.yudao.module.bpm.controller.admin.comment.vo;

import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程评论 Response VO")
@Data
public class BpmCommentRespVO {

    @Schema(description = "评论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "任务编号", example = "2048")
    private String taskId;

    @Schema(description = "任务")
    private Task task;

    @Schema(description = "流程实例编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    private String processInstanceId;

    @Schema(description = "评论类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private String type;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "请关注报销发票附件")
    private String message;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private UserSimpleBaseVO user;

    @Schema(description = "任务")
    @Data
    public static class Task {

        @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
        private String id;

        @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "经理审批")
        private String name;

        @Schema(description = "任务定义的标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "Activity_one")
        private String taskDefinitionKey;

    }

}
