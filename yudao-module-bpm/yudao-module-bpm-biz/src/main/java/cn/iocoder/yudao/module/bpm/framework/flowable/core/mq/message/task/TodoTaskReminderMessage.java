package cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.message.task;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 待办任务提醒消息
 *
 * @author jason
 */
@Data
public class TodoTaskReminderMessage {

    /**
     * 租户 Id
     */
    @NotNull(message = "租户 Id 不能未空")
    private Long tenantId;

    /**
     * 用户Id
     */
    @NotNull(message = "用户 Id 不能未空")
    private Long userId;

    /**
     * 任务名称
     */
    @NotEmpty(message = "任务名称不能未空")
    private String taskName;

    // TODO 暂时只有站内信通知. 后面可以增加
}
