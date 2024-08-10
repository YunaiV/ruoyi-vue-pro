package cn.iocoder.yudao.framework.ai.image;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.ai.zhipuai.ZhiPuAiImageOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;

/**
 * {@link ZhiPuAiImageModel} 集成测试
 */
public class ZhiPuAiImageModelTests {

    private final ZhiPuAiImageApi imageApi = new ZhiPuAiImageApi(
            "78d3228c1d9e5e342a3e1ab349e2dd7b.VXLoq5vrwK2ofboy");
    private final ZhiPuAiImageModel imageModel = new ZhiPuAiImageModel(imageApi);

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        ZhiPuAiImageOptions imageOptions = ZhiPuAiImageOptions.builder()
                .withModel(ZhiPuAiImageApi.ImageModel.CogView_3.getValue())
                .build();
        ImagePrompt prompt = new ImagePrompt("万里长城", imageOptions);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
