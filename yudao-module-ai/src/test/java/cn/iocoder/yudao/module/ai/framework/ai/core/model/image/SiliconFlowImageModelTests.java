package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * {@link SiliconFlowImageModel} 集成测试
 */
public class SiliconFlowImageModelTests {

    private final SiliconFlowImageModel imageModel = new SiliconFlowImageModel(
            new SiliconFlowImageApi("sk-epsakfenqnyzoxhmbucsxlhkdqlcbnimslqoivkshalvdozz") // 密钥
    );

    @Test
    @Disabled
    public void testCall() {
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

    @Test
    public void testCall_imageSize() {
        // mock 硅基流动接口：断言实际请求体带上宽高对应的 image_size
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        server.expect(requestTo(endsWith("v1/images/generations")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"model\":\"Qwen/Qwen-Image\",\"image_size\":\"1664x928\"}"))
                .andRespond(withSuccess("{\"created\":1,\"data\":[{\"url\":\"https://example.com/1.png\"}]}",
                        MediaType.APPLICATION_JSON));
        SiliconFlowImageModel model = new SiliconFlowImageModel(
                new SiliconFlowImageApi("https://api.siliconflow.cn", "sk-test", restClientBuilder));
        // 准备参数：与 AiImageUtils 一致，通过 builder 设置宽高
        SiliconFlowImageOptions imageOptions = SiliconFlowImageOptions.builder()
                .model("Qwen/Qwen-Image").width(1664).height(928).build();

        // 调用
        ImageResponse response = model.call(new ImagePrompt("年度报告封面", imageOptions));
        // 断言
        server.verify();
        assertEquals("https://example.com/1.png", response.getResult().getOutput().getUrl());
    }

}
