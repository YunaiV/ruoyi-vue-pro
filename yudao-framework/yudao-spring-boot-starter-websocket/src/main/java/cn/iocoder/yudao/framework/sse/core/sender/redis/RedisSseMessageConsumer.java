package cn.iocoder.yudao.framework.sse.core.sender.redis;

import cn.iocoder.yudao.framework.mq.redis.core.pubsub.AbstractRedisChannelMessageListener;
import lombok.RequiredArgsConstructor;

/**
 * {@link RedisSseMessage} 广播消息的消费者，投递到本节点的 SSE 连接
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class RedisSseMessageConsumer extends AbstractRedisChannelMessageListener<RedisSseMessage> {

    private final RedisSseMessageSender redisSseMessageSender;

    @Override
    public void onMessage(RedisSseMessage message) {
        redisSseMessageSender.deliver(message.getMessage());
    }

}
