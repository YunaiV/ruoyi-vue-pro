package cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.consumer.task;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.message.task.TodoTaskReminderMessage;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *  待办任务提醒 - 站内信的消费者
 *
 * @author jason
 */
@Component
@Slf4j
public class SysNotifyTodoTaskReminderConsumer {

    private static final String TASK_REMIND_TEMPLATE_CODE = "user_task_remind";

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @EventListener
    @Async
    public void onMessage(TodoTaskReminderMessage message) {
        log.info("站内信消费者接收到消息 [消息内容({})] ", message);
        TenantUtils.execute(message.getTenantId(), ()-> {
            Map<String,Object> templateParams = MapUtil.newHashMap();
            templateParams.put("name", message.getTaskName());
            NotifySendSingleToUserReqDTO req = new NotifySendSingleToUserReqDTO().setUserId(message.getUserId())
                    .setTemplateCode(TASK_REMIND_TEMPLATE_CODE).setTemplateParams(templateParams);
            notifyMessageSendApi.sendSingleMessageToAdmin(req);
        });
    }
}
