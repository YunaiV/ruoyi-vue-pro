package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "管理后台 - 流程实例的进度 Response VO")
@Data
public class BpmProcessInstanceProgressRespVO {

    @Schema(description = "流程实例的状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 BpmProcessInstanceStatusEnum 枚举

    private List<ProcessNodeProgress> nodeProgressList;

    @Schema(description = "节点进度信息")
    @Data
    public static class ProcessNodeProgress {

        @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "StartUserNode")
        private String id;  // Bpmn XML 节点 Id

        @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "发起人")
        private String name;

        @Schema(description = "节点展示内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "指定成员: 芋道源码")
        private String displayText;

        @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer nodeType; // 参见 BpmSimpleModelNodeType 枚举

        // TODO @jason：可以复用 BpmTaskStatusEnum 么？非必要不加太多状态枚举哈
        @Schema(description = "节点状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Integer status; // 参见 BpmProcessNodeProgressEnum 枚举

        @Schema(description = "节点的开始时间")
        private LocalDateTime startTime;
        @Schema(description = "节点的结束时间")
        private LocalDateTime endTime;

        @Schema(description = "用户列表")
        private List<User> userList;

        // TODO @jason：如果条件信息，怎么展示哈？
        @Schema(description = "分支节点")
        private List<ProcessNodeProgress> branchNodes;  // 有且仅有条件、并行、包容节点才会有分支节点

        // TODO 用户意见，评论

    }

    @Schema(description = "用户信息")
    @Data
    public static class User {

        @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
        private String nickname;

        @Schema(description = "用户头像", example = "芋艿")
        private String avatar;

        // TODO @jason：是不是把 processed 和 userTaskStatus 合并？

        @Schema(description = "是否已处理", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean processed;

        @Schema(description = "用户任务的处理状态", example = "1")
        private Integer userTaskStatus;

    }

}
