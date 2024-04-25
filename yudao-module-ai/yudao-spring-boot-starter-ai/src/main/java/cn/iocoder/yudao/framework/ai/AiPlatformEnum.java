package cn.iocoder.yudao.framework.ai;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public enum AiPlatformEnum {


    YI_YAN("yiyan", "一言"),
    QIAN_WEN("qianwen", "千问"),
    XING_HUO("xinghuo", "星火"),

    ;

    private String platform;
    private String name;

    public static AiPlatformEnum valueOfPlatform(String platform) {
        for (AiPlatformEnum itemEnum : AiPlatformEnum.values()) {
            if (itemEnum.getPlatform().equals(platform)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + platform);
    }

}
