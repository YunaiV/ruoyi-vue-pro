package cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("流程活动的 Response VO")
@Data
public class BpmActivityRespVO {

    @ApiModelProperty(value = "流程活动的标识", required = true, example = "1024")
    private String key;
    @ApiModelProperty(value = "流程活动的类型", required = true, example = "StartEvent")
    private String type;

    @ApiModelProperty(value = "流程活动的开始时间", required = true)
    private Date startTime;
    @ApiModelProperty(value = "流程活动的结束时间", required = true)
    private Date endTime;

    /**
     * 关联的流程任务，只有 UserTask 类型才有
     */
    private Task task;

    @ApiModel(value = "流程任务")
    @Data
    public static class Task {

        @ApiModelProperty(value = "关联的流程任务的编号", required = true, example = "2048")
        private String id;
        @ApiModelProperty(value = "关联的流程任务的结果", required = true, example = "2", notes = "参见 bpm_process_instance_result 枚举")
        private Integer result;

    }

}
