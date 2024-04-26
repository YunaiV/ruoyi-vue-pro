package cn.iocoder.yudao.framework.ai.chatqianwen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 千问 chat 模型
 *
 * 模型地址：https://help.aliyun.com/document_detail/2712576.html
 *
 * @author fansili
 * @time 2024/4/26 10:15
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum QianWenChatModal {

    qwen_turbo("通义千问超大规模语言模型", "qwen-turbo"),
    qwen_plus("通义千问超大规模语言模型增强版", "qwen-plus"),
    qwen_max("通义千问千亿级别超大规模语言模型", "qwen-max"),
    qwen_max_0403("通义千问千亿级别超大规模语言模型-0403", "qwen-max-0403"),
    qwen_max_0107("通义千问千亿级别超大规模语言模型-0107", "qwen-max-0107"),
    qwen_max_1201("通义千问千亿级别超大规模语言模型-1201", "qwen-max-1201"),
    qwen_max_longcontext("通义千问千亿级别超大规模语言模型-28k tokens", "qwen-max-longcontext"),

    ;

    private String name;

    private String value;

}
