package cn.iocoder.yudao.framework.ai.image;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.web.client.RestClient;

/**
 * {@link OpenAiImageModel} 集成测试类
 *
 * @author fansili
 */
public class OpenAiImageModelTests {

    private final OpenAiImageApi imageApi = new OpenAiImageApi(
            "https://api.holdai.top",
            "sk-dZEPiVaNcT3FHhef51996bAa0bC74806BeAb620dA5Da10Bf",
            RestClient.builder());
    private final OpenAiImageModel imageModel = new OpenAiImageModel(imageApi);

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        ImageOptions options = OpenAiImageOptions.builder()
                .withModel(OpenAiImageApi.ImageModel.DALL_E_2.getValue()) // 这个模型比较便宜
                .withHeight(256).withWidth(256)
                .build();
        ImagePrompt prompt = new ImagePrompt("中国长城!", options);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
