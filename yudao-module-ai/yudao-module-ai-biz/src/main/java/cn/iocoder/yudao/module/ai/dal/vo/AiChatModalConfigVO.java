package cn.iocoder.yudao.module.ai.dal.vo;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import cn.iocoder.yudao.framework.ai.core.model.xinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.core.model.yiyan.api.YiYanChatModel;

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
     * 参考：{@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型类型(冗余，方便类型转换)
     * {@link YiYanChatModel}
     * {@link XingHuoChatModel}
     */
    private String type;
}
