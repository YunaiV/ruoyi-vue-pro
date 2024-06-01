package cn.iocoder.yudao.module.ai.enums.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO done @fansili：1）类注释要加下；2）author 和 time 用 javadoc，@author 和 @since；3）@AllArgsConstructor 使用这个注解，去掉构造方法；4）value 改成 model 字段，然后注释都写下哈；5）message 改成 name，然后注释都写下哈
// TODO @fan: AiModelEnum 是不是可以缩写成这个哈；所有的模型，都写在这里枚举；
/**
 * @author: fansili
 * @time: 2024/3/4 12:36
 */
@Getter
@AllArgsConstructor
public enum AiModelEnum {

    // open ai
    OPEN_AI_GPT_3_5( "GPT3.5", "gpt-3.5-turbo",null),
    OPEN_AI_GPT_4("GPT4", "gpt-4-turbo",null),

    // 千问付费模型
    QWEN_TURBO("通义千问超大规模语言模型", "qwen-turbo", null),
    QWEN_PLUS("通义千问超大规模语言模型增强版", "qwen-plus", null),
    QWEN_MAX("通义千问千亿级别超大规模语言模型", "qwen-max", null),
    QWEN_MAX_0403("通义千问千亿级别超大规模语言模型-0403", "qwen-max-0403", null),
    QWEN_MAX_0107("通义千问千亿级别超大规模语言模型-0107", "qwen-max-0107", null),
    QWEN_MAX_1201("通义千问千亿级别超大规模语言模型-1201", "qwen-max-1201", null),
    QWEN_MAX_LONGCONTEXT("通义千问千亿级别超大规模语言模型-28k tokens", "qwen-max-longcontext", null),

    // 千问开源模型
    // https://help.aliyun.com/document_detail/2666503.html?spm=a2c4g.2701795.0.0.26eb34dfKzcWN4
    QWEN_72B_CHAT("通义千问1.5对外开源的72B规模参数量的经过人类指令对齐的chat模型", "qwen-72b-chat", null),

    // 一言模型
    ERNIE4_0("ERNIE 4.0", "ERNIE 4.0", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro"),
    ERNIE4_3_5_8K("ERNIE-3.5-8K", "ERNIE-3.5-8K", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions"),
    ERNIE4_3_5_8K_0205("ERNIE-3.5-8K-0205", "ERNIE-3.5-8K-0205", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-0205"),

    ERNIE4_3_5_8K_1222("ERNIE-3.5-8K-1222", "ERNIE-3.5-8K-1222", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-8k-1222"),
    ERNIE4_BOT_8K("ERNIE-Bot-8K", "ERNIE-Bot-8K", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_bot_8k"),
    ERNIE4_3_5_4K_0205("ERNIE-3.5-4K-0205", "ERNIE-3.5-4K-0205", "/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-3.5-4k-0205"),

//    文档地址：https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
//    general指向V1.5版本;
//    generalv2指向V2版本;
//    generalv3指向V3版本;
//    generalv3.5指向V3.5版本;

    XING_HUO_1_5("星火大模型1.5",  "general", "/v1.1/chat"),
    XING_HUO_2_0("星火大模型2.0", "generalv2", "/v2.1/chat"),
    XING_HUO_3_0("星火大模型3.0", "generalv3", "/v3.1/chat"),
    XING_HUO_3_5("星火大模型3.5", "generalv3.5", "/v3.5/chat"),
    ;

    /**
     * 模型名字 - 用于展示
     */
    private final String name;
    /**
     * 模型标志 - 用于参数传递
     */
    private final String model;
    /**
     * uri地址
     */
    private final String uri;

}
