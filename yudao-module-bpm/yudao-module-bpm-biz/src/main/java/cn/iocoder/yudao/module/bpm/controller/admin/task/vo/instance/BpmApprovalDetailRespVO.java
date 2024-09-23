package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "管理后台 - 审批详情 Response VO")
@Data
public class BpmApprovalDetailRespVO {

    @Schema(description = "流程实例的状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 BpmProcessInstanceStatusEnum 枚举

    @Schema(description = "审批信息列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ApprovalNodeInfo> approveNodes;

    @Schema(description = "审批节点信息")
    @Data
    public static class ApprovalNodeInfo {

        @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "StartUserNode")
        private String id;

        @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "发起人")
        private String name;

        @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer nodeType; // 参见 BpmSimpleModelNodeType 枚举

        @Schema(description = "节点状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Integer status; // 参见 BpmTaskStatusEnum 枚举

        @Schema(description = "节点的开始时间")
        private LocalDateTime startTime;
        @Schema(description = "节点的结束时间")
        private LocalDateTime endTime;

        @Schema(description = "审批节点的任务信息")
        private List<ApprovalTaskInfo> tasks;

        @Schema(description = "候选人用户列表")
        private List<User> candidateUserList; // 用于未运行任务节点

    }

    @Schema(description = "用户信息")
    @Data
    public static class User {

        @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
        private String nickname;

        @Schema(description = "用户头像", example = "https://www.iocoder.cn/1.png")
        private String avatar;

    }

    @Schema(description = "审批任务信息")
    @Data
    public static class ApprovalTaskInfo {

        @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private String id;

        @Schema(description = "任务所属人", example = "1024")
        private User ownerUser;

        @Schema(description = "任务分配人", example = "2048")
        private User assigneeUser;

        @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer status;  // 参见 BpmTaskStatusEnum 枚举

        @Schema(description = "审批意见", example = "同意")
        private String reason;

    }

}
