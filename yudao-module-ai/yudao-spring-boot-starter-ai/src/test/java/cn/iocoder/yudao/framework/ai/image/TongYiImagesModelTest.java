package cn.iocoder.yudao.framework.ai.image;

import com.alibaba.cloud.ai.tongyi.image.TongYiImagesModel;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;

/**
 * {@link com.alibaba.cloud.ai.tongyi.image.TongYiImagesModel} 集成测试类
 *
 * @author fansili
 */
public class TongYiImagesModelTest {

    private final ImageSynthesis imageApi = new ImageSynthesis();
    private final TongYiImagesModel imageModel = new TongYiImagesModel(imageApi);

    static {
        Constants.apiKey = "sk-Zsd81gZYg7";
    }

    @Test
    @Disabled
    public void imageCallTest() {
        // 准备参数
        ImageOptions options = OpenAiImageOptions.builder()
                .withModel(ImageSynthesis.Models.WANX_V1)
                .withHeight(256).withWidth(256)
                .build();
        ImagePrompt prompt = new ImagePrompt("中国长城!", options);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
