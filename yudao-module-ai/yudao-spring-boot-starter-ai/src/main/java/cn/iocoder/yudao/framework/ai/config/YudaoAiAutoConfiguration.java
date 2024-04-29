package cn.iocoder.yudao.framework.ai.config;

import cn.hutool.core.io.IoUtil;
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
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageApi;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageClient;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageOptions;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.api.MidjourneyInteractionsApi;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.MidjourneyWebSocketStarter;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.listener.MidjourneyMessageListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
        qianWenOptions.setTopK(qianWenProperties.getTopK());
        qianWenOptions.setTopP(qianWenProperties.getTopP());
        qianWenOptions.setMaxTokens(qianWenProperties.getMaxTokens());
        qianWenOptions.setTemperature(qianWenProperties.getTemperature());
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
        yiYanOptions.setTopK(yiYanProperties.getTopK());
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
        // 创建 client
        return new OpenAiImageClient(
                new OpenAiImageApi(openAiImageProperties.getApiKey()),
                new OpenAiImageOptions()
                        .setModel(openAiImageProperties.getModel())
                        .setResponseFormat(OpenAiImageOptions.ResponseFormatEnum.URL.getValue())
                        .setStyle(openAiImageProperties.getStyle())
        );
    }

    @Bean
    @ConditionalOnProperty(value = "yudao.ai.midjourney.enable", havingValue = "true")
    public MidjourneyWebSocketStarter midjourneyWebSocketStarter(ApplicationContext applicationContext, YudaoAiProperties yudaoAiProperties) {
        // 获取 midjourneyProperties
        YudaoAiProperties.MidjourneyProperties midjourneyProperties = yudaoAiProperties.getMidjourney();
        // 获取 midjourneyConfig
        MidjourneyConfig midjourneyConfig = getMidjourneyConfig(applicationContext, midjourneyProperties);
        // 创建 socket messageListener
        MidjourneyMessageListener messageListener = new MidjourneyMessageListener(midjourneyConfig);
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