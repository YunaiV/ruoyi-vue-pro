package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// TODO @芋艿：是不是要融合？
@Schema(description = "管理后台 - 流程实例的分页 Item Response VO")
@Data
public class BpmProcessInstancePageItemRespVO {

    @Schema(description = "流程实例的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private String processDefinitionId;

    @Schema(description = "流程分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String category;
    @Schema(description = "流程分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "请假")
    private String categoryName;

    @Schema(description = "流程实例的状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 BpmProcessInstanceStatusEnum 枚举

    @Schema(description = "提交时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    /**
     * 当前任务
     */
    private List<Task> tasks;

    @Schema(description = "流程任务")
    @Data
    public static class Task {

        @Schema(description = "流程任务的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private String id;

        @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
        private String name;

    }

}
