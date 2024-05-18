package cn.iocoder.yudao.framework.ai.core.model.yiyan.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文心一言的模型枚举
 *
 * 可参考 <a href="https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t">百度文档</>
 *
 * @author fansili
 */
@Getter
@AllArgsConstructor
public enum YiYanChatModel {

    ERNIE4_0("ERNIE 4.0", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro"),
    ERNIE4_3_5_8K("ERNIE-3.5-8K", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions"),
    ERNIE4_3_5_8K_0205("ERNIE-3.5-8K-0205", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-0205"),
    ERNIE4_3_5_8K_1222("ERNIE-3.5-8K-1222", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-1222"),
    ERNIE4_BOT_8K("ERNIE-Bot-8K", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_bot_8k"),
    ERNIE4_3_5_4K_0205("ERNIE-3.5-4K-0205", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-4k-0205"),
    ;

    /**
     * 模型名
     */
    private final String model;
    /**
     * API URL
     */
    private final String uri;

    public static YiYanChatModel valueOfModel(String model) {
        for (YiYanChatModel modelEnum : YiYanChatModel.values()) {
            if (modelEnum.getModel().equals(model)) {
                return modelEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + model);
    }
}
