package cn.iocoder.yudao.module.ai.dal.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * modal config
 *
 * @author fansili
 * @time 2024/5/6 15:06
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatModalConfigVO {

    /**
     * 模型平台 (冗余，方便类型转换)
     * 参考：{@link cn.iocoder.yudao.framework.ai.AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型类型(冗余，方便类型转换)
     * {@link cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatModel}
     * {@link cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatModel}
     */
    private String type;
}
