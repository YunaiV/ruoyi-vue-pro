package cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.producer.task;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.message.task.TodoTaskReminderMessage;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

// TODO @jason：建议直接调用 BpmMessageService 哈；更简化一点~
/**
 * 待办任务提醒 Producer
 *
 * @author jason
 */
@Component
@Validated
public class TodoTaskReminderProducer {

    @Resource
    private ApplicationContext applicationContext;

    public void sendReminderMessage(@Valid TodoTaskReminderMessage message) {
        applicationContext.publishEvent(message);
    }

}
