package cn.iocoder.yudao.framework.ai.config;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactory;
import cn.iocoder.yudao.framework.ai.core.factory.AiClientFactoryImpl;
import cn.iocoder.yudao.framework.ai.core.model.suno.SunoConfig;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.AceDataSunoApi;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.models.midjourney.MidjourneyConfig;
import org.springframework.ai.models.midjourney.MidjourneyMessage;
import org.springframework.ai.models.midjourney.api.MidjourneyInteractionsApi;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyMessageHandler;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyWebSocketStarter;
import org.springframework.ai.models.midjourney.webSocket.listener.MidjourneyMessageListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    @ConditionalOnMissingBean(value = MidjourneyMessageHandler.class)
    public MidjourneyMessageHandler defaultMidjourneyMessageHandler() {
        // 如果没有实现 MidjourneyMessageHandler 默认注入一个
        return new MidjourneyMessageHandler() {
            @Override
            public void messageHandler(MidjourneyMessage midjourneyMessage) {
                log.info("default midjourney message: {}", midjourneyMessage);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.midjourney.enable", havingValue = "true")
    public MidjourneyWebSocketStarter midjourneyWebSocketStarter(ApplicationContext applicationContext,
                                                                 MidjourneyMessageHandler midjourneyMessageHandler,
                                                                 YudaoAiProperties yudaoAiProperties) {
        // 获取 midjourneyProperties
        YudaoAiProperties.MidjourneyProperties midjourneyProperties = yudaoAiProperties.getMidjourney();
        // 获取 midjourneyConfig
        MidjourneyConfig midjourneyConfig = getMidjourneyConfig(applicationContext, midjourneyProperties);
        // 创建 socket messageListener
        MidjourneyMessageListener messageListener = new MidjourneyMessageListener(midjourneyConfig, midjourneyMessageHandler);
        // 创建 MidjourneyWebSocketStarter
        return new MidjourneyWebSocketStarter(midjourneyProperties.getWssUrl(), null, midjourneyConfig, messageListener);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.midjourney.enable", havingValue = "true")
    public MidjourneyInteractionsApi midjourneyInteractionsApi(ApplicationContext applicationContext, YudaoAiProperties yudaoAiProperties) {
        // 获取 midjourneyConfig
        MidjourneyConfig midjourneyConfig = getMidjourneyConfig(applicationContext, yudaoAiProperties.getMidjourney());
        // 创建 MidjourneyInteractionsApi
        return new MidjourneyInteractionsApi(midjourneyConfig);
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.suno.enable", havingValue = "true")
    public AceDataSunoApi sunoApi(YudaoAiProperties yudaoAiProperties) {
        return new AceDataSunoApi(new SunoConfig(yudaoAiProperties.getSuno().getToken()));
    }

    private static @NotNull MidjourneyConfig getMidjourneyConfig(ApplicationContext applicationContext,
                                                                 YudaoAiProperties.MidjourneyProperties midjourneyProperties) {
        Map<String, String> requestTemplates = new HashMap<>();
        try {
            Resource[] resources = applicationContext.getResources("classpath:http-body/*.json");
            for (var resource : resources) {
                String filename = resource.getFilename();
                String params = IoUtil.readUtf8(resource.getInputStream());
                requestTemplates.put(filename.substring(0, filename.length() - 5), params);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Midjourney json模板初始化出错! " + e.getMessage());
        }
        // 创建 midjourneyConfig
        return new MidjourneyConfig(midjourneyProperties.getToken(),
                midjourneyProperties.getGuildId(), midjourneyProperties.getChannelId(), requestTemplates);
    }
}