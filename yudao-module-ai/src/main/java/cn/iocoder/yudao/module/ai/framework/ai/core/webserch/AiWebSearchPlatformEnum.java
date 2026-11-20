package cn.iocoder.yudao.module.ai.framework.ai.core.webserch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 联网搜索平台的枚举
 *
 * 配置项 yudao.ai.web-search.platform 填小写即可，例如 deepseek
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiWebSearchPlatformEnum {

    DEEPSEEK("DeepSeek"), // 搜索 + 网页读取，默认
    BOCHA("博查"), // 搜索 + 网页读取
    BAIDU("百度"), // 只有搜索，使用千帆的 API Key
    PARALLEL("Parallel"); // 搜索 + 网页读取，匿名访问 MCP，无需 API Key

    /**
     * 名字
     */
    private final String name;

}
