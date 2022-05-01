package cn.iocoder.yudao.module.bpm.service.message.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * BPM 发送任务被分配 Request DTO
 */
@Data
public class BpmMessageSendWhenTaskCreatedReqDTO {

    /**
     * 流程实例的编号
     */
    @NotEmpty(message = "流程实例的编号不能为空")
    private String processInstanceId;
    /**
     * 流程实例的名字
     */
    @NotEmpty(message = "流程实例的名字不能为空")
    private String processInstanceName;
    @NotNull(message = "发起人的用户编号")
    private Long startUserId;
    @NotEmpty(message = "发起人的昵称")
    private String startUserNickname;

    /**
     * 流程任务的编号
     */
    @NotEmpty(message = "流程任务的编号不能为空")
    private String taskId;
    /**
     * 流程任务的名字
     */
    @NotEmpty(message = "流程任务的名字不能为空")
    private String taskName;

    /**
     * 审批人的用户编号
     */
    @NotNull(message = "审批人的用户编号不能为空")
    private Long assigneeUserId;

}
