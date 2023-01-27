package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 流程活动的 Response VO")
@Data
public class BpmActivityRespVO {

    @ApiModelProperty(value = "流程活动的标识", required = true, example = "1024")
    private String key;
    @ApiModelProperty(value = "流程活动的类型", required = true, example = "StartEvent")
    private String type;

    @ApiModelProperty(value = "流程活动的开始时间", required = true)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "流程活动的结束时间", required = true)
    private LocalDateTime endTime;

    @ApiModelProperty(value = "关联的流程任务的编号", example = "2048", notes = "关联的流程任务，只有 UserTask 等类型才有")
    private String taskId;

}
