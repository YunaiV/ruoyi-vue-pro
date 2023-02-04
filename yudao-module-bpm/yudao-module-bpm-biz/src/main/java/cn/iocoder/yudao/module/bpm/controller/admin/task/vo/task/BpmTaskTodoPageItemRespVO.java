package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程任务的 Running 进行中的分页项 Response VO")
@Data
public class BpmTaskTodoPageItemRespVO {

    @Schema(description = "任务编号", required = true, example = "1024")
    private String id;

    @Schema(description = "任务名字", required = true, example = "芋道")
    private String name;

    @Schema(description = "接收时间", required = true)
    private LocalDateTime claimTime;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "激活状态-参见 SuspensionState 枚举", required = true, example = "1")
    private Integer suspensionState;

    /**
     * 所属流程实例
     */
    private ProcessInstance processInstance;

    @Data
    @Schema(description = "流程实例")
    public static class ProcessInstance {

        @Schema(description = "流程实例编号", required = true, example = "1024")
        private String id;

        @Schema(description = "流程实例名称", required = true, example = "芋道")
        private String name;

        @Schema(description = "发起人的用户编号", required = true, example = "1024")
        private Long startUserId;

        @Schema(description = "发起人的用户昵称", required = true, example = "芋艿")
        private String startUserNickname;

        @Schema(description = "流程定义的编号", required = true, example = "2048")
        private String processDefinitionId;

    }

}
