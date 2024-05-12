package org.springframework.ai.models.tongyi;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 千问 chat 模型
 *
 * 模型地址：https://help.aliyun.com/document_detail/2712576.html
 * 模型介绍：https://help.aliyun.com/document_detail/2666503.html?spm=a2c4g.2701795.0.0.26eb34dfKzcWN4
 *
 * @author fansili
 * @time 2024/4/26 10:15
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum QianWenChatModal {

    // 千问付费模型
    QWEN_TURBO("通义千问超大规模语言模型", "qwen-turbo"),
    QWEN_PLUS("通义千问超大规模语言模型增强版", "qwen-plus"),
    QWEN_MAX("通义千问千亿级别超大规模语言模型", "qwen-max"),
    QWEN_MAX_0403("通义千问千亿级别超大规模语言模型-0403", "qwen-max-0403"),
    QWEN_MAX_0107("通义千问千亿级别超大规模语言模型-0107", "qwen-max-0107"),
    QWEN_MAX_1201("通义千问千亿级别超大规模语言模型-1201", "qwen-max-1201"),
    QWEN_MAX_LONGCONTEXT("通义千问千亿级别超大规模语言模型-28k tokens", "qwen-max-longcontext"),

    // 开源模型
    // https://help.aliyun.com/document_detail/2666503.html?spm=a2c4g.2701795.0.0.26eb34dfKzcWN4
    QWEN_72B_CHAT("通义千问1.5对外开源的72B规模参数量的经过人类指令对齐的chat模型", "qwen-72b-chat"),

    ;

    private String name;

    private String model;

    public static QianWenChatModal valueOfModel(String model) {
        for (QianWenChatModal itemEnum : QianWenChatModal.values()) {
            if (itemEnum.getModel().equals(model)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + model);
    }
}
