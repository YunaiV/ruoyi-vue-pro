package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
    public XingHuoChatClient xingHuoChatClient(YudaoAiProperties yudaoAiProperties) {
        return new XingHuoChatClient(
                new XingHuoApi(
                        yudaoAiProperties.getXingHuo().getAppId(),
                        yudaoAiProperties.getXingHuo().getAppKey(),
                        yudaoAiProperties.getXingHuo().getSecretKey()
                ),
                new XingHuoOptions().setChatModel(yudaoAiProperties.getXingHuo().getChatModel())
        );
    }
}
