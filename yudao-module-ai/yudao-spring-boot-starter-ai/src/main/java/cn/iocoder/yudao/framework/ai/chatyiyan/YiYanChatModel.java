package cn.iocoder.yudao.framework.ai.chatyiyan;

import lombok.Getter;

/**
 * 一言模型
 *
 * 可参考百度文档：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/clntwmv7t
 *
 * author: fansili
 * time: 2024/3/9 12:01
 */
@Getter
public enum YiYanChatModel {

    ERNIE4_0("ERNIE 4.0", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro"),
    ERNIE4_3_5_8K("ERNIE-3.5-8K", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions"),
    ERNIE4_3_5_8K_0205("ERNIE-3.5-8K-0205", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-0205"),

    ERNIE4_3_5_8K_1222("ERNIE-3.5-8K-1222", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-1222"),
    ERNIE4_BOT_8K("ERNIE-Bot-8K", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_bot_8k"),
    ERNIE4_3_5_4K_0205("ERNIE-3.5-4K-0205", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-4k-0205"),

    ;

    YiYanChatModel(String value, String uri) {
        this.value = value;
        this.uri = uri;
    }

    private String value;

    private String uri;

}
