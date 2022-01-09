package cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("流程任务的 Done 已完成的分页项 Response VO")
@Data
public class BpmTaskDonePageItemRespVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "任务名字", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "接收时间", required = true)
    private Date claimTime;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;
    @ApiModelProperty(value = "结束时间", required = true)
    private Date endTime;
    @ApiModelProperty(value = "持续时间", required = true, example = "1000")
    private Long durationInMillis;

    @ApiModelProperty(value = "任务结果", required = true, notes = "参见 bpm_process_instance_result", example = "2")
    private Integer result;
    @ApiModelProperty(value = "审批建议", required = true, example = "不请假了！")
    private String comment;

    /**
     * 所属流程实例
     */
    private ProcessInstance processInstance;

    @Data
    @ApiModel("流程实例")
    public static class ProcessInstance {

        @ApiModelProperty(value = "流程实例编号", required = true, example = "1024")
        private String id;

        @ApiModelProperty(value = "流程实例名称", required = true, example = "芋道")
        private String name;

        @ApiModelProperty(value = "发起人的用户编号", required = true, example = "1024")
        private Long startUserId;

        @ApiModelProperty(value = "发起人的用户昵称", required = true, example = "芋艿")
        private String startUserNickname;

        @ApiModelProperty(value = "流程定义的编号", required = true, example = "2048")
        private String processDefinitionId;

    }


    // 任务编号、流程名称、任务节点、流程发起人、接收时间、审批时间、耗时【名称、开始时间】「流程记录、撤回」
    // 任务编号、任务名称、所属流程、委托代办人、流程发起人、优先级、审批操作、审批意见、耗时、创建时间【名称、开始时间】「申请详情」

    // 任务编号、任务名称、流程名称、流程发起人、接收时间、审批时间、耗时【名称、接收时间】「详情」TODO 撤回

}
