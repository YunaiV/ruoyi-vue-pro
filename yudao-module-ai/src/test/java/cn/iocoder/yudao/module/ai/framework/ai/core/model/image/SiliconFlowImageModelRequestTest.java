package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * {@link SiliconFlowImageModel} 的单元测试类：断言合并参数后，实际提交给硅基流动的请求体
 *
 * 使用 {@link MockRestServiceServer}，不会请求真实的硅基流动接口
 *
 * @author 芋道源码
 */
public class SiliconFlowImageModelRequestTest {

    private static final String REFERENCE_IMAGE = "data:image/png;base64,aGVsbG8=";

    private MockRestServiceServer server;
    private SiliconFlowImageModel imageModel;

    @BeforeEach
    public void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        imageModel = new SiliconFlowImageModel(new SiliconFlowImageApi("http://127.0.0.1", "sk-test", restClientBuilder));
    }

    @Test
    public void testCall_widthHeightAndReferenceImage() {
        // mock 接口：断言请求体带上宽高对应的 image_size，以及参考图 image
        expectRequest("{\"prompt\":\"一只猫\",\"model\":\"Kwai-Kolors/Kolors\",\"image_size\":\"1024x768\","
                + "\"image\":\"" + REFERENCE_IMAGE + "\"}");
        // 准备参数
        SiliconFlowImageOptions options = SiliconFlowImageOptions.builder().model("Kwai-Kolors/Kolors")
                .width(1024).height(768).image(REFERENCE_IMAGE).build();

        // 调用
        imageModel.call(new ImagePrompt("一只猫", options));
        // 断言
        server.verify();
    }

    @Test
    public void testCall_imageSizeOnly() {
        // mock 接口：断言未设置宽高时，直接设置的 image_size 也会提交
        expectRequest("{\"prompt\":\"一只猫\",\"model\":\"Kwai-Kolors/Kolors\",\"image_size\":\"960x1280\"}");
        // 准备参数
        SiliconFlowImageOptions options = SiliconFlowImageOptions.builder().model("Kwai-Kolors/Kolors")
                .imageSize("960x1280").build();

        // 调用
        imageModel.call(new ImagePrompt("一只猫", options));
        // 断言
        server.verify();
    }

    private void expectRequest(String expectedJson) {
        server.expect(requestTo(endsWith("v1/images/generations")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(expectedJson))
                .andRespond(withSuccess("{\"created\":1,\"data\":[{\"url\":\"https://example.com/1.png\"}]}",
                        MediaType.APPLICATION_JSON));
    }

}
