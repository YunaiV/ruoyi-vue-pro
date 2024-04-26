package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatModal;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenOptions;
import cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenApi;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoApi;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatClient;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanOptions;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ai 自动配置
 *
 * @author fansili
 * @time 2024/4/12 16:29
 * @since 1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(YudaoAiProperties.class)
public class YudaoAiAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.xinghuo.enable", havingValue = "true")
    public XingHuoChatClient xingHuoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.XingHuoProperties xingHuoProperties = yudaoAiProperties.getXinghuo();
        return new XingHuoChatClient(
                new XingHuoApi(
                        xingHuoProperties.getAppId(),
                        xingHuoProperties.getAppKey(),
                        xingHuoProperties.getSecretKey()
                ),
                new XingHuoOptions().setChatModel(xingHuoProperties.getChatModel())
        );
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.qianwen.enable", havingValue = "true")
    public QianWenChatClient qianWenChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.QianWenProperties qianWenProperties = yudaoAiProperties.getQianwen();
        return new QianWenChatClient(
                new QianWenApi(
                        qianWenProperties.getAgentKey(),
                        QianWenChatModal.QWEN_72B_CHAT
                ),
                new QianWenOptions()
                        .setAppId(qianWenProperties.getAppId())
        );
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.yiyan.enable", havingValue = "true")
    public YiYanChatClient yiYanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.YiYanProperties yiYanProperties = yudaoAiProperties.getYiyan();
        return new YiYanChatClient(
                new YiYanApi(
                        yiYanProperties.getAppKey(),
                        yiYanProperties.getSecretKey(),
                        yiYanProperties.getChatModel(),
                        yiYanProperties.getRefreshTokenSecondTime()
                ),
                new YiYanOptions().setMax_output_tokens(2048));
    }
}