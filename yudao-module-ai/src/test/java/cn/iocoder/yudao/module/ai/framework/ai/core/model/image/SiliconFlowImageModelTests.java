package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;

import static cn.iocoder.yudao.module.ai.util.AiUtils.validateApiKey;

/**
 * {@link SiliconFlowImageModel} 集成测试
 */
public class SiliconFlowImageModelTests {

    private static final String API_KEY = SystemUtil.get("SILICONFLOW_API_KEY",
            "sk-xxxx"); // 按需改成你的 SiliconFlow API Key

    private final SiliconFlowImageModel imageModel = new SiliconFlowImageModel(
            new SiliconFlowImageApi(API_KEY)
    );

    @Test
    @Disabled
    public void testCall() {
        validateApiKey(API_KEY);
        // 准备参数
        SiliconFlowImageOptions imageOptions = SiliconFlowImageOptions.builder()
                .model("Kwai-Kolors/Kolors")
                .build();
        ImagePrompt prompt = new ImagePrompt("万里长城", imageOptions);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
