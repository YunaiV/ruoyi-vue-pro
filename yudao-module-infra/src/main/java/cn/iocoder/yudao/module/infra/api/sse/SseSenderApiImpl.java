package cn.iocoder.yudao.module.infra.api.sse;

import cn.iocoder.yudao.framework.sse.core.sender.SseMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SSE 发送器的 API 实现类
 *
 * @author 芋道源码
 */
@Component
public class SseSenderApiImpl implements SseSenderApi {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.sse.enable 配置项默认关闭 SSE 的功能，所以这里只能不强制注入
    private SseMessageSender sseMessageSender;

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        if (sseMessageSender == null) {
            return;
        }
        sseMessageSender.send(userType, userId, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        if (sseMessageSender == null) {
            return;
        }
        sseMessageSender.send(userType, messageType, messageContent);
    }

    @Override
    public void send(String connectionId, String messageType, String messageContent) {
        if (sseMessageSender == null) {
            return;
        }
        sseMessageSender.send(connectionId, messageType, messageContent);
    }

}
