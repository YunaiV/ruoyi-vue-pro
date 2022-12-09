package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(title = "管理后台 - 流程实例的分页 Item Response VO")
@Data
public class BpmProcessInstancePageItemRespVO {

    @Schema(title = "流程实例的编号", required = true, example = "1024")
    private String id;

    @Schema(title = "流程名称", required = true, example = "芋道")
    private String name;

    @Schema(title = "流程定义的编号", required = true, example = "2048")
    private String processDefinitionId;

    @Schema(title = "流程分类", required = true, description = "参见 bpm_model_category 数据字典", example = "1")
    private String category;

    @Schema(title = "流程实例的状态", required = true, description = "参见 bpm_process_instance_status", example = "1")
    private Integer status;

    @Schema(title = "流程实例的结果", required = true, description = "参见 bpm_process_instance_result", example = "2")
    private Integer result;

    @Schema(title = "提交时间", required = true)
    private LocalDateTime createTime;

    @Schema(title = "结束时间", required = true)
    private LocalDateTime endTime;

    /**
     * 当前任务
     */
    private List<Task> tasks;

    @Schema(title = "流程任务")
    @Data
    public static class Task {

        @Schema(title = "流程任务的编号", required = true, example = "1024")
        private String id;

        @Schema(title = "任务名称", required = true, example = "芋道")
        private String name;

    }

}
