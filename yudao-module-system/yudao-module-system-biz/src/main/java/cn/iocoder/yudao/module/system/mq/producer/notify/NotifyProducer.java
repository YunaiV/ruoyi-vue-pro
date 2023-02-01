package cn.iocoder.yudao.module.system.mq.producer.notify;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.system.mq.message.notify.NotifyTemplateRefreshMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Notify 站内信相关消息的 Producer
 *
 * @author xrcoder
 * @since 2022-08-06
 */
@Slf4j
@Component
public class NotifyProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;


    /**
     * 发送 {@link NotifyTemplateRefreshMessage} 消息
     */
    public void sendNotifyTemplateRefreshMessage() {
        NotifyTemplateRefreshMessage message = new NotifyTemplateRefreshMessage();
        redisMQTemplate.send(message);
    }


}
