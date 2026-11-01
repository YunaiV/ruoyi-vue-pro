package cn.iocoder.yudao.module.ai.util;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AiUtils} 的单元测试类
 *
 * 重点：通义的 multiModel 按本次 Prompt 是否含 Media 选择请求通道，不受型号名单限制
 *
 * @author 芋道源码
 */
public class AiUtilsTest {

    /**
     * 自定义型号：不在 {@link AiUtils#TONG_YI_MULTI_MODELS} 名单中
     */
    private static final String CUSTOM_MODEL = "my-qwen-vl";
    /**
     * 名单内的型号
     */
    private static final String LISTED_MODEL = "qwen3-vl-plus";

    @Test
    public void testBuildChatOptions_tongYiCustomModelWithMedia() {
        // 调用
        DashScopeChatOptions options = (DashScopeChatOptions) AiUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, true);
        // 断言：含 Media 走多模态接口
        assertEquals(Boolean.TRUE, options.getMultiModel());
    }

    @Test
    public void testBuildChatOptions_tongYiCustomModelTextOnly() {
        // 调用
        DashScopeChatOptions options = (DashScopeChatOptions) AiUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                CUSTOM_MODEL, 0.7, 1024, null, null, false);
        // 断言：纯文本保持原通道
        assertEquals(Boolean.FALSE, options.getMultiModel());
    }

    @Test
    public void testBuildChatOptions_tongYiListedModelTextOnly() {
        // 调用：原有重载，默认不含 Media
        DashScopeChatOptions options = (DashScopeChatOptions) AiUtils.buildChatOptions(AiPlatformEnum.TONG_YI,
                LISTED_MODEL, 0.7, 1024);
        // 断言：名单内型号纯文本仍按名单走多模态接口，与改动前一致
        assertEquals(Boolean.TRUE, options.getMultiModel());
    }

}
