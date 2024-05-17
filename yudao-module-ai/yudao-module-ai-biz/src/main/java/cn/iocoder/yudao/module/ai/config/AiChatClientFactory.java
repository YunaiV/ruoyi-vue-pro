package cn.iocoder.yudao.module.ai.config;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.models.tongyi.QianWenChatClient;
import org.springframework.ai.models.xinghuo.XingHuoChatClient;
import org.springframework.ai.models.yiyan.YiYanChatClient;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * factory
 *
 * @author fansili
 * @time 2024/4/25 17:36
 * @since 1.0
 */
@Component
public class AiChatClientFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public ChatClient getChatClient(AiPlatformEnum platformEnum) {
        if (AiPlatformEnum.QIAN_WEN == platformEnum) {
            return applicationContext.getBean(QianWenChatClient.class);
        } else if (AiPlatformEnum.YI_YAN == platformEnum) {
            return applicationContext.getBean(YiYanChatClient.class);
        } else if (AiPlatformEnum.XING_HUO == platformEnum) {
            return applicationContext.getBean(XingHuoChatClient.class);
        }
        throw new IllegalArgumentException("不支持的 chat client!");
    }

    // TODO yunai 要不再加一个接口，让他们拥有 ChatClient、StreamingChatClient 功能
    public StreamingChatClient getStreamingChatClient(AiPlatformEnum platformEnum) {
//        if (true) {
//            return applicationContext.getBean(OllamaChatClient.class);
//        }
        if (AiPlatformEnum.QIAN_WEN == platformEnum) {
            return applicationContext.getBean(QianWenChatClient.class);
        } else if (AiPlatformEnum.YI_YAN == platformEnum) {
            return applicationContext.getBean(YiYanChatClient.class);
        } else if (AiPlatformEnum.XING_HUO == platformEnum) {
            return applicationContext.getBean(XingHuoChatClient.class);
        }  else if (AiPlatformEnum.OLLAMA == platformEnum) {
            return applicationContext.getBean(OllamaChatClient.class);
        }
        throw new IllegalArgumentException("不支持的 chat client!");
    }
}
