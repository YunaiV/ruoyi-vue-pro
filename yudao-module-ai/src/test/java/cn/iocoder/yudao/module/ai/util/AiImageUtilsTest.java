package cn.iocoder.yudao.module.ai.util;

import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.openai.OpenAiImageOptions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiImageUtils} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiImageUtilsTest {

    @Test
    public void testBuildImageOptions_openAi() {
        // 调用：dall-e 指定 b64_json
        ImageOptions dallE = AiImageUtils.buildImageOptions(AiPlatformEnum.OPENAI, "dall-e-3", 1792, 1024);
        // 断言
        assertInstanceOf(OpenAiImageOptions.class, dallE);
        assertEquals("dall-e-3", dallE.getModel());
        assertEquals(1792, dallE.getWidth());
        assertEquals(1024, dallE.getHeight());
        assertEquals("b64_json", dallE.getResponseFormat());

        // 调用：gpt-image 不支持 response_format 参数
        ImageOptions gptImage = AiImageUtils.buildImageOptions(AiPlatformEnum.OPENAI, "gpt-image-1", 1536, 1024);
        // 断言
        assertEquals(1536, gptImage.getWidth());
        assertNull(gptImage.getResponseFormat());
    }

    @Test
    public void testBuildImageOptions_siliconFlow() {
        // 调用
        ImageOptions options = AiImageUtils.buildImageOptions(AiPlatformEnum.SILICONFLOW, "Qwen/Qwen-Image", 1664, 928);
        // 断言
        assertInstanceOf(SiliconFlowImageOptions.class, options);
        assertEquals("Qwen/Qwen-Image", options.getModel());
        assertEquals(1664, options.getWidth());
        assertEquals(928, options.getHeight());
    }

    @Test
    public void testBuildImageOptions_tongYi() {
        // 调用
        ImageOptions options = AiImageUtils.buildImageOptions(AiPlatformEnum.ALIBABA, "wanx2.1-t2i-turbo", 1280, 720);
        // 断言
        assertInstanceOf(DashScopeImageOptions.class, options);
        assertEquals("wanx2.1-t2i-turbo", options.getModel());
        assertEquals(1, options.getN());
        assertEquals(1280, options.getWidth());
        assertEquals(720, options.getHeight());
    }

    @Test
    public void testBuildImageOptions_notSupported() {
        // 调用，并断言异常
        assertThrows(IllegalArgumentException.class,
                () -> AiImageUtils.buildImageOptions(AiPlatformEnum.MIDJOURNEY, "midjourney", 1024, 1024));
    }

}
