package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author kemengkai
 * @create 2022-05-07 08:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BpmTaskBackReqVO {

    @ApiModelProperty(value = "用户id", required = true, example = "1")
    @NotEmpty(message = "用户id不能为空")
    private String userId;

    @ApiModelProperty(value = "流程编号id", required = true, example = "730da750-cc4f-11ec-b58e-1e429355e4a0")
    @NotEmpty(message = "流程编号id不能为空")
    private String procInstId;

    @ApiModelProperty(value = "当前任务id", required = true, example = "730da750-cc4f-11ec-b58e-1e429355e4a0")
    @NotEmpty(message = "当前任务id不能为空")
    private String taskId;

    @ApiModelProperty(value = "当前流程任务id", required = true, example = "Activity_1jlembv")
    @NotNull(message = "当前流程任务id不能为空")
    private String oldTaskDefKey;

    @ApiModelProperty(value = "准备回退的流程任务id", required = true, example = "task01")
    @NotNull(message = "准备回退流程任务id不能为空")
    private String newTaskDefKey;

    @ApiModelProperty(value = "审批结果", required = true, example = "任务驳回")
    @NotNull(message = "审批结果")
    private String reason;
}
