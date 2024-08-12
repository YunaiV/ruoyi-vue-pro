package cn.iocoder.yudao.framework.ai.image;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.QianFanImageOptions;
import org.springframework.ai.qianfan.api.QianFanImageApi;

import static cn.iocoder.yudao.framework.ai.image.StabilityAiImageModelTests.viewImage;

/**
 * {@link QianFanImageModel} 集成测试类
 */
public class QianFanImageTests {

    private final QianFanImageApi imageApi = new QianFanImageApi(
            "qS8k8dYr2nXunagK4SSU8Xjj", "pHGbx51ql2f0hOyabQvSZezahVC3hh3e");
    private final QianFanImageModel imageModel = new QianFanImageModel(imageApi);

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        // 只支持 1024x1024、768x768、768x1024、1024x768、576x1024、1024x576
        QianFanImageOptions imageOptions = QianFanImageOptions.builder()
                .withModel(QianFanImageApi.ImageModel.Stable_Diffusion_XL.getValue())
                .withWidth(1024).withHeight(1024)
                .withN(1)
                .build();
        ImagePrompt prompt = new ImagePrompt("good", imageOptions);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        String b64Json = response.getResult().getOutput().getB64Json();
        System.out.println(response);
        viewImage(b64Json);
    }

}
