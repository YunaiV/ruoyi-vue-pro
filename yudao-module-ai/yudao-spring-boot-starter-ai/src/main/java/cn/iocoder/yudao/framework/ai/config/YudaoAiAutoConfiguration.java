package cn.iocoder.yudao.framework.ai.config;

import cn.hutool.core.io.IoUtil;
import org.springframework.ai.models.tongyi.QianWenChatClient;
import org.springframework.ai.models.tongyi.QianWenChatModal;
import org.springframework.ai.models.tongyi.QianWenOptions;
import org.springframework.ai.models.tongyi.api.QianWenApi;
import org.springframework.ai.models.xinghuo.XingHuoChatClient;
import org.springframework.ai.models.xinghuo.XingHuoOptions;
import org.springframework.ai.models.xinghuo.api.XingHuoApi;
import org.springframework.ai.models.yiyan.YiYanChatClient;
import org.springframework.ai.models.yiyan.YiYanOptions;
import org.springframework.ai.models.yiyan.api.YiYanApi;
import org.springframework.ai.models.midjourney.MidjourneyConfig;
import org.springframework.ai.models.midjourney.MidjourneyMessage;
import org.springframework.ai.models.midjourney.api.MidjourneyInteractionsApi;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyMessageHandler;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyWebSocketStarter;
import org.springframework.ai.models.midjourney.webSocket.listener.MidjourneyMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;
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
 * ai 自动配置
 *
 * @author fansili
 * @time 2024/4/12 16:29
 * @since 1.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(YudaoAiProperties.class)
public class YudaoAiAutoConfiguration {

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
        YiYanOptions yiYanOptions = new YiYanOptions();
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
    @ConditionalOnProperty(value = "yudao.ai.openAiImage.enable", havingValue = "true")
    public OpenAiImageClient openAiImageClient(YudaoAiProperties yudaoAiProperties) {
        YudaoAiProperties.OpenAiImageProperties openAiImageProperties = yudaoAiProperties.getOpenAiImage();
        OpenAiImageOptions openAiImageOptions = new OpenAiImageOptions();
        openAiImageOptions.setModel(openAiImageProperties.getModel().getModel());
        openAiImageOptions.setStyle(openAiImageProperties.getStyle().getStyle());
        openAiImageOptions.setResponseFormat("url"); // TODO 芋艿：OpenAiImageOptions.ResponseFormatEnum.URL.getValue()
        // 创建 client
        return new OpenAiImageClient(
                new OpenAiImageApi(openAiImageProperties.getApiKey()),
                openAiImageOptions,
                RetryUtils.DEFAULT_RETRY_TEMPLATE);
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