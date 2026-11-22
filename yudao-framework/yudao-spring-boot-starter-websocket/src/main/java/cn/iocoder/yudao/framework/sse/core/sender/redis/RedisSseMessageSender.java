package cn.iocoder.yudao.framework.sse.core.sender.redis;

import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;

/**
 * 基于 Redis 的 {@link SseMessageSender} 实现类
 *
 * @author 芋道源码
 */
public class RedisSseMessageSender extends SseMessageSender {

    private final RedisMQTemplate redisMQTemplate;

    public RedisSseMessageSender(SseEmitterSessionManager sessionManager, long sendTimeoutMillis, int maxSendFailures,
                                 RedisMQTemplate redisMQTemplate) {
        super(sessionManager, sendTimeoutMillis, maxSendFailures);
        this.redisMQTemplate = redisMQTemplate;
    }

    @Override
    protected void doPublish(SseMessage message) {
        redisMQTemplate.send(new RedisSseMessage().setMessage(message));
    }

}
