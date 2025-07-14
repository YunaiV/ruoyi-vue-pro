package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;

/**
 * {@link OpenAiImageModel} 集成测试类
 *
 * @author fansili
 */
public class OpenAiImageModelTests {

    private final OpenAiImageModel imageModel = new OpenAiImageModel(OpenAiImageApi.builder()
            .baseUrl("https://api.holdai.top") // apiKey
            .apiKey("sk-PytRecQlmjEteoa2RRN6cGnwslo72UUPLQVNEMS6K9yjbmpD")
            .build());

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        ImageOptions options = OpenAiImageOptions.builder()
                .model(OpenAiImageApi.ImageModel.DALL_E_2.getValue()) // 这个模型比较便宜
                .height(256).width(256)
                .build();
        ImagePrompt prompt = new ImagePrompt("中国长城!", options);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
