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
 * TODO @芋艿：注：spring-ai-alibaba-dashscope（1.1.2.2）的 {@code DashScopeImageApi#resolveImagePath} 未给 {@code wan2.7-image} 加路由分支，
 * 会落到默认的 {@code text2image/image-synthesis} 异步端点 + 旧版 {@code prompt} 入参，
 * 而该模型实际要求 {@code multimodal-generation/generation} 同步端点 + {@code messages} 入参，
 * 端点、异步头、入参结构全部对不上，所以走 SDK 直接调用必失败。
 *
 * 临时方案：改用 SDK 已支持的 {@code wan2.6-image}（异步）或 {@code qwen-image}（同步）；
 * 或在项目内同包同名覆盖 {@code DashScopeImageApi}，把 {@code wan2.7*} 也路由到 {@code wan2.6-image} 那条 {@code image-generation/generation} 异步分支。
 *
 * @author fansili
 */
public class TongYiImagesModelTest {

    private final DashScopeImageModel imageModel = DashScopeImageModel.builder()
            .dashScopeApi(DashScopeImageApi.builder()
                    .apiKey("sk-cd9f39e99ea54840bd1888282325f55a") // https://bailian.console.aliyun.com/cn-beijing/?tab=model#/api-key 获取密钥
                    .build())
            .build();

    // TODO @芋艿：
    @Test
    @Disabled
    public void imageCallTest() {
        // 准备参数
        ImageOptions options = DashScopeImageOptions.builder()
                .model("wan2.7-image")
//                .withSize("2k")
                .height(768).width(768)
                .n(1)
                .build();
        ImagePrompt prompt = new ImagePrompt("中国长城!", options);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        System.out.println(response);
    }

}
