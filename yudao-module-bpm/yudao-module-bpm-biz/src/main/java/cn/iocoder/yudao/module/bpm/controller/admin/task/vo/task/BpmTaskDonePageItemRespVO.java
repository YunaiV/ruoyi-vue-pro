package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程任务的 Done 已完成的分页项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskDonePageItemRespVO extends BpmTaskTodoPageItemRespVO {

    @Schema(description = "结束时间", required = true)
    private LocalDateTime endTime;
    @Schema(description = "持续时间", required = true, example = "1000")
    private Long durationInMillis;

    @Schema(description = "任务结果-参见 bpm_process_instance_result", required = true, example = "2")
    private Integer result;
    @Schema(description = "审批建议", required = true, example = "不请假了！")
    private String reason;

}
