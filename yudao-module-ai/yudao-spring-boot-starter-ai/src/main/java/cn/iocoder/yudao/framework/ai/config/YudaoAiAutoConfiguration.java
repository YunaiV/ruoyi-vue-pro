package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactory;
import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactoryImpl;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenChatClient;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenChatModal;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenOptions;
import cn.iocoder.yudao.framework.ai.core.model.tongyi.api.QianWenApi;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.api.XingHuoApi;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.YiYanChatClient;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.YiYanChatOptions;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 芋道 AI 自动配置
 *
 * @author fansili
 */
@AutoConfiguration
@EnableConfigurationProperties(YudaoAiProperties.class)
@Slf4j
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
    @ConditionalOnProperty(value = "yudao.ai.qianwen.enable", havingValue = "true")
    public QianWenChatClient qianWenChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.QianWenProperties qianWenProperties = yudaoAiProperties.getQianwen();
        // 转换配置
        QianWenOptions qianWenOptions = new QianWenOptions();
        qianWenOptions.setModel(qianWenProperties.getModel().getModel());
        qianWenOptions.setTemperature(qianWenProperties.getTemperature());
//        qianWenOptions.setTopK(qianWenProperties.getTopK()); TODO 芋艿：后续弄
        qianWenOptions.setTopP(qianWenProperties.getTopP());
        qianWenOptions.setMaxTokens(qianWenProperties.getMaxTokens());
//        qianWenOptions.setTemperature(qianWenProperties.getTemperature()); TODO 芋艿：后续弄
        return new QianWenChatClient(
                new QianWenApi(
                        qianWenProperties.getApiKey(),
                        QianWenChatModal.QWEN_72B_CHAT
                ),
                qianWenOptions
        );
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.yiyan.enable", havingValue = "true")
    public YiYanChatClient yiYanChatClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.YiYanProperties yiYanProperties = yudaoAiProperties.getYiyan();
        // 转换配置
        YiYanChatOptions yiYanOptions = new YiYanChatOptions();
//        yiYanOptions.setTopK(yiYanProperties.getTopK()); TODO 芋艿：后续弄
        yiYanOptions.setTopP(yiYanProperties.getTopP());
        yiYanOptions.setTemperature(yiYanProperties.getTemperature());
        yiYanOptions.setMaxOutputTokens(yiYanProperties.getMaxTokens());
        return new YiYanChatClient(
                new YiYanApi(
                        yiYanProperties.getAppKey(),
                        yiYanProperties.getSecretKey(),
                        yiYanProperties.getModel(),
                        yiYanProperties.getRefreshTokenSecondTime()
                ),
                yiYanOptions
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