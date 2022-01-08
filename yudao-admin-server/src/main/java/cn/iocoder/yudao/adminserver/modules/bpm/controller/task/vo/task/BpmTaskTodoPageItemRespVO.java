package cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "流程任务的 Running 进行中的分页项 Response VO", description = "待办任务")
@Data
public class BpmTaskTodoPageItemRespVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "任务名字", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "接收时间", required = true)
    private Date claimTime;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "激活状态", required = true, example = "1", notes = "参见 SuspensionState 枚举")
    private Integer suspensionState;

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

    // 任务编号、任务名称、任务节点、流程版本、流程发起人、接收时间【名称、开始时间】「处理」
    // 任务编号、任务名称、所属流程、委托代办人、流程发起人、优先级、状态、创建时间【名称、开始时间】「申请详情、通过、驳回、转办、历史」

    // 任务编号、任务名称、流程名称、流程发起人、接收时间、状态【名称、接收时间】【处理、委托】
}
