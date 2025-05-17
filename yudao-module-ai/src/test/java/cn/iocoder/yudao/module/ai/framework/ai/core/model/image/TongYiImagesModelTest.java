package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;

/**
 * {@link DashScopeImageModel} 集成测试类
 *
 * @author fansili
 */
public class TongYiImagesModelTest {

    private final DashScopeImageModel imageModel = new DashScopeImageModel(
            new DashScopeImageApi("sk-7d903764249848cfa912733146da12d1"));

    @Test
    @Disabled
    public void imageCallTest() {
        // 准备参数
        ImageOptions options = DashScopeImageOptions.builder()
                .withModel("wanx-v1")
                .withHeight(256).withWidth(256)
                .build();
        ImagePrompt prompt = new ImagePrompt("中国长城!", options);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
