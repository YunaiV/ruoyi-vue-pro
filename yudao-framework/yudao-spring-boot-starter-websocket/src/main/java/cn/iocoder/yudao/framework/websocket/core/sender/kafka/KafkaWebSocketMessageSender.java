package cn.iocoder.yudao.framework.websocket.core.sender.kafka;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketPushMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ExecutionException;

/**
 * 基于 Kafka 的 {@link WebSocketMessageSender} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class KafkaWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    private final String topic;

    public KafkaWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                       KafkaTemplate<Object, Object> kafkaTemplate,
                                       String topic) {
        super(sessionManager);
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * 通过 Kafka 广播消息
     *
     * @param message 消息
     */
    @Override
    protected void doPublish(WebSocketPushMessage message) {
        KafkaWebSocketMessage mqMessage = BeanUtils.toBean(message, KafkaWebSocketMessage.class);
        try {
            kafkaTemplate.send(topic, mqMessage).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[doPublish][等待 Kafka 发送结果时被中断]", e);
        } catch (ExecutionException e) {
            log.error("[doPublish][发送消息({}) 到 Kafka 失败]", mqMessage, e);
        }
    }

}
