package cn.iocoder.yudao.framework.sse.core.sender.rocketmq;

import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

/**
 * 基于 RocketMQ 的 {@link SseMessageSender} 实现类
 *
 * @author 芋道源码
 */
public class RocketMQSseMessageSender extends SseMessageSender {

    private final RocketMQTemplate rocketMQTemplate;

    private final String topic;

    public RocketMQSseMessageSender(SseEmitterSessionManager sessionManager, long sendTimeoutMillis, int maxSendFailures,
                                    RocketMQTemplate rocketMQTemplate, String topic) {
        super(sessionManager, sendTimeoutMillis, maxSendFailures);
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
    }

    @Override
    protected void doPublish(SseMessage message) {
        rocketMQTemplate.syncSend(topic, message);
    }

}
