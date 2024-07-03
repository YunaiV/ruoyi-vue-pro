package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactory;
import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactoryImpl;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoApi;
import com.alibaba.cloud.ai.tongyi.TongYiAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 芋道 AI 自动配置
 *
 * @author fansili
 */
@AutoConfiguration
@EnableConfigurationProperties(YudaoAiProperties.class)
@Slf4j
@Import(TongYiAutoConfiguration.class)
public class YudaoAiAutoConfiguration {

    @Bean
    public AiClientFactory aiClientFactory() {
        return new AiClientFactoryImpl();
    }

    // ========== 各种 AI Client 创建 ==========

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.xinghuo.enable", havingValue = "true")
    public XingHuoChatClient xingHuoChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.XingHuoProperties xingHuoProperties = yudaoAiProperties.getXinghuo();
        // 转换配置
        XingHuoOptions xingHuoOptions = new XingHuoOptions();
        xingHuoOptions.setChatModel(xingHuoProperties.getModel());
        xingHuoOptions.setTopK(xingHuoProperties.getTopK());
        xingHuoOptions.setTemperature(xingHuoProperties.getTemperature());
        xingHuoOptions.setMaxTokens(xingHuoProperties.getMaxTokens());
        return new XingHuoChatClient(
                new XingHuoApi(
                        xingHuoProperties.getAppId(),
                        xingHuoProperties.getAppKey(),
                        xingHuoProperties.getSecretKey()
                ),
                xingHuoOptions
        );
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.midjourney.enable", havingValue = "true")
    public MidjourneyApi midjourneyApi(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.MidjourneyProperties config = yudaoAiProperties.getMidjourney();
        return new MidjourneyApi(config.getBaseUrl(), config.getApiKey(), config.getNotifyUrl());
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.suno.enable", havingValue = "true")
    public SunoApi sunoApi(YudaoAiProperties yudaoAiProperties) {
        return new SunoApi(yudaoAiProperties.getSuno().getBaseUrl());
    }

}