package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 流程任务的 Done 已完成的分页项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskDonePageItemRespVO extends BpmTaskTodoPageItemRespVO {

    @Schema(title = "结束时间", required = true)
    private LocalDateTime endTime;
    @Schema(title = "持续时间", required = true, example = "1000")
    private Long durationInMillis;

    @Schema(title = "任务结果", required = true, description = "参见 bpm_process_instance_result", example = "2")
    private Integer result;
    @Schema(title = "审批建议", required = true, example = "不请假了！")
    private String reason;

}
