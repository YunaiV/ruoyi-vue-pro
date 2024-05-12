package org.springframework.ai.models.xinghuo;

import lombok.Getter;

/**
 * 讯飞星火 模型
 *
 * 文档地址：https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
 *
 * 1tokens 约等于1.5个中文汉字 或者 0.8个英文单词
 * 星火V1.5支持[搜索]内置插件；星火V2.0、V3.0和V3.5支持[搜索]、[天气]、[日期]、[诗词]、[字词]、[股票]六个内置插件
 * 星火V3.5 现已支持system、Function Call 功能。
 *
 * author: fansili
 * time: 2024/3/11 10:12
 */
@Getter
public enum XingHuoChatModel {

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

    XingHuoChatModel(String name, String model, String uri) {
        this.name = name;
        this.model = model;
        this.uri = uri;
    }

    private String name;

    private String model;

    private String uri;

    public static XingHuoChatModel valueOfModel(String model) {
        for (XingHuoChatModel itemEnum : XingHuoChatModel.values()) {
            if (itemEnum.getModel().equals(model)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + model);
    }
}
