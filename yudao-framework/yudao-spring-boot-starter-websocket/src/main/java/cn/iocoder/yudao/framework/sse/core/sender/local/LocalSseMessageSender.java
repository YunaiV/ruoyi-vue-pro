package cn.iocoder.yudao.framework.sse.core.sender.local;

import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;

/**
 * 本地的 {@link SseMessageSender} 实现类
 *
 * 注意：仅仅适合单机场景！！！
 *
 * @author 芋道源码
 */
public class LocalSseMessageSender extends SseMessageSender {

    public LocalSseMessageSender(SseEmitterSessionManager sessionManager, long sendTimeoutMillis, int maxSendFailures) {
        super(sessionManager, sendTimeoutMillis, maxSendFailures);
    }

    @Override
    protected void doPublish(SseMessage message) {
        deliver(message);
    }

}
