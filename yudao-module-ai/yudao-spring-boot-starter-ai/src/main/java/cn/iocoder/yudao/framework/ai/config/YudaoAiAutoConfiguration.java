package cn.iocoder.yudao.framework.ai.config;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenOptions;
import cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenApi;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatClient;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.chatxinghuo.api.XingHuoApi;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatClient;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanOptions;
import cn.iocoder.yudao.framework.ai.chatyiyan.api.YiYanApi;
import cn.iocoder.yudao.framework.ai.exception.AiException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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
    @ConditionalOnMissingBean(value = AiClient.class)
    public AiClient aiClient(YudaoAiProperties yudaoAiProperties) {
        Map<String, Object> chatClientMap = buildChatClientMap(yudaoAiProperties);
        return new YudaoAiClient(chatClientMap);
    }

    public Map<String, Object> buildChatClientMap(YudaoAiProperties yudaoAiProperties) {
        Map<String, Object> chatMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> properties : yudaoAiProperties.entrySet()) {
            String beanName = properties.getKey();
            Map<String, Object> aiPlatformMap = properties.getValue();

            // 检查平台类型是否正确
            String aiPlatform = String.valueOf(aiPlatformMap.get("aiPlatform"));
            if (!AiPlatformEnum.mapValues.containsKey(aiPlatform)) {
                throw new AiException("AI平台名称错误! 可以参考 AiPlatformEnum 类!");
            }
            // 获取平台类型
            AiPlatformEnum aiPlatformEnum = AiPlatformEnum.mapValues.get(aiPlatform);
            // 获取 chat properties
            YudaoAiProperties.ChatProperties chatProperties = getChatProperties(aiPlatformEnum, aiPlatformMap);
            // 创建客户端
            Object chatClient = createChatClient(chatProperties);
            chatMap.put(beanName, chatClient);
        }
        return chatMap;
    }

    private static Object createChatClient(YudaoAiProperties.ChatProperties chatProperties) {
        if (AiPlatformEnum.XING_HUO == chatProperties.getAiPlatform()) {
            YudaoAiProperties.XingHuoProperties xingHuoProperties = (YudaoAiProperties.XingHuoProperties) chatProperties;
            return new XingHuoChatClient(
                    new XingHuoApi(
                            xingHuoProperties.getAppId(),
                            xingHuoProperties.getAppKey(),
                            xingHuoProperties.getSecretKey()
                    ),
                    new XingHuoOptions().setChatModel(xingHuoProperties.getChatModel())
            );
        } else if (AiPlatformEnum.QIAN_WEN == chatProperties.getAiPlatform()) {
            YudaoAiProperties.QianWenProperties qianWenProperties = (YudaoAiProperties.QianWenProperties) chatProperties;
            return new QianWenChatClient(
                    new QianWenApi(
                            qianWenProperties.getAccessKeyId(),
                            qianWenProperties.getAccessKeySecret(),
                            qianWenProperties.getAgentKey(),
                            qianWenProperties.getEndpoint()
                    ),
                    new QianWenOptions()
                            .setAppId(qianWenProperties.getAppId())
            );
        } else if (AiPlatformEnum.YI_YAN == chatProperties.getAiPlatform()) {
            YudaoAiProperties.YiYanProperties yiYanProperties = (YudaoAiProperties.YiYanProperties) chatProperties;
            return new YiYanChatClient(
                    new YiYanApi(
                            yiYanProperties.getAppKey(),
                            yiYanProperties.getSecretKey(),
                            yiYanProperties.getChatModel(),
                            yiYanProperties.getRefreshTokenSecondTime()
                    ),
                    new YiYanOptions().setMax_output_tokens(2048));
        }
        throw new AiException("不支持的Ai类型!");
    }


    private static YudaoAiProperties.ChatProperties getChatProperties(AiPlatformEnum aiPlatformEnum, Map<String, Object> aiPlatformMap) {
        if (AiPlatformEnum.XING_HUO == aiPlatformEnum) {
            YudaoAiProperties.XingHuoProperties xingHuoProperties = new YudaoAiProperties.XingHuoProperties();
            BeanUtil.fillBeanWithMap(aiPlatformMap, xingHuoProperties, true);
            return xingHuoProperties;
        } else if (AiPlatformEnum.YI_YAN == aiPlatformEnum) {
            YudaoAiProperties.YiYanProperties yiYanProperties = new YudaoAiProperties.YiYanProperties();
            BeanUtil.fillBeanWithMap(aiPlatformMap, yiYanProperties, true);
            return yiYanProperties;
        } else if (AiPlatformEnum.QIAN_WEN == aiPlatformEnum) {
            YudaoAiProperties.QianWenProperties qianWenProperties = new YudaoAiProperties.QianWenProperties();
            BeanUtil.fillBeanWithMap(aiPlatformMap, qianWenProperties, true);
            return qianWenProperties;
        }
        throw new AiException("不支持的Ai类型!");
    }
}