package cn.iocoder.yudao.module.ai.framework.ai.core.model;

import cn.hutool.core.util.StrUtil;

/**
 * AI 模型集成测试的工具类
 *
 * @author 芋道源码
 */
public class AiModelTestUtils {

    /**
     * 校验 API Key，避免集成测试使用默认占位值发起调用。
     *
     * @param apiKey API Key
     */
    public static void validateApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey) || "sk-xxxx".equals(apiKey)) {
            throw new IllegalStateException("apiKey 不能为空");
        }
    }

}
