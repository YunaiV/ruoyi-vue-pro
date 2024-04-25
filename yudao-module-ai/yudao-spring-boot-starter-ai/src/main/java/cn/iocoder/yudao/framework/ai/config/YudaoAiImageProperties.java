package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoOptions;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ai 自动配置
 *
 * @author fansili
 * @time 2024/4/12 16:29
 * @since 1.0
 */

@Data
@Accessors(chain = true)
public class YudaoAiImageProperties extends LinkedHashMap<String, Map<String, Object>> {

    private String initType;
    private QianWenProperties qianwen;
    private XingHuoOptions xinghuo;
    private YiYanProperties yiyan;

    @Data
    @Accessors(chain = true)
    public static class QianWenProperties extends ChatProperties {
        /**
         * 阿里云：服务器接入点
         */
        private String endpoint = "bailian.cn-beijing.aliyuncs.com";
        /**
         * 阿里云：权限 accessKeyId
         */
        private String accessKeyId;
        /**
         * 阿里云：权限 accessKeySecret
         */
        private String accessKeySecret;
        /**
         * 阿里云：agentKey
         */
        private String agentKey;
        /**
         * 阿里云：agentKey(相当于应用id)
         */
        private String appId;

    }

    @Data
    @Accessors(chain = true)
    public static class XingHuoProperties extends ChatProperties {
        private String appId;
        private String appKey;
        private String secretKey;
        private XingHuoChatModel chatModel;
    }

    @Data
    @Accessors(chain = true)
    public static class YiYanProperties extends ChatProperties {
        /**
         * appKey
         */
        private String appKey;
        /**
         * secretKey
         */
        private String secretKey;
        /**
         * 模型
         */
        private YiYanChatModel chatModel = YiYanChatModel.ERNIE4_3_5_8K;
        /**
         * token 刷新时间(默认 86400 = 24小时)
         */
        private int refreshTokenSecondTime = 86400;
    }

    @Data
    @Accessors(chain = true)
    public static class ChatProperties {

        private AiPlatformEnum aiPlatform;

        private Float temperature;

        private Float topP;

        private Integer topK;
    }
}
