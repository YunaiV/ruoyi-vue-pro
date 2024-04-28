package cn.iocoder.yudao.framework.ai.imageopenai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.ai.chat.ChatException;
import cn.iocoder.yudao.framework.ai.chatyiyan.exception.YiYanApiException;
import cn.iocoder.yudao.framework.ai.image.*;
import cn.iocoder.yudao.framework.ai.imageopenai.api.OpenAiImageRequest;
import cn.iocoder.yudao.framework.ai.imageopenai.api.OpenAiImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

/**
 * open ai 生成 image
 *
 * author: fansili
 * time: 2024/3/17 09:51
 */
@Slf4j
public class OpenAiImageClient implements ImageClient {

    /**
     * open image ai
     */
    private OpenAiImageApi openAiImageApi;
    /**
     * 默认使用的 ImageOptions
     */
    private OpenAiImageOptions defaultImageOptions;


    public final RetryTemplate retryTemplate = RetryTemplate.builder()
            // 最大重试次数 10
            .maxAttempts(10)
            .retryOn(YiYanApiException.class)
            // 最大重试5次，第一次间隔3000ms，第二次3000ms * 2，第三次3000ms * 3，以此类推，最大间隔3 * 60000ms
            .exponentialBackoff(Duration.ofMillis(3000), 2, Duration.ofMillis(3 * 60000))
            .withListener(new RetryListener() {
                @Override
                public <T extends Object, E extends Throwable> void onError(RetryContext context,
                                                                            RetryCallback<T, E> callback, Throwable throwable) {
                    log.warn("重试异常:" + context.getRetryCount(), throwable);
                };
            })
            .build();

    public OpenAiImageClient(OpenAiImageApi openAiImageApi, OpenAiImageOptions defaultImageOptions) {
        this.openAiImageApi = openAiImageApi;
        this.defaultImageOptions = defaultImageOptions;
    }

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {
        return this.retryTemplate.execute(ctx -> {
            OpenAiImageOptions openAiImageOptions = getOpenAiImageOptions(imagePrompt);
            // 创建请求
            OpenAiImageRequest request = new OpenAiImageRequest();
            BeanUtil.copyProperties(openAiImageOptions, request);
            request.setPrompt(imagePrompt.getInstructions().get(0).getText());
            request.setModel(openAiImageOptions.getModel());
            request.setStyle(openAiImageOptions.getStyle().getStyle());
            request.setSize(openAiImageOptions.getSize());
            // 发送请求
            OpenAiImageResponse response = openAiImageApi.createImage(request);
            return new ImageResponse(response.getData().stream().map(res -> {
                byte[] bytes = HttpUtil.downloadBytes(res.getUrl());
                String base64 = Base64.encode(bytes);
                return new ImageGeneration(new Image(res.getUrl(), base64));
            }).toList());
        });
    }

    private @NotNull OpenAiImageOptions getOpenAiImageOptions(ImagePrompt imagePrompt) {
        // 检查是否配置了 OpenAiImageOptions
        if (defaultImageOptions == null && imagePrompt.getOptions() == null) {
            throw new ChatException("OpenAiImageOptions 未配置参数!");
        }
        // 优先使用 request 中的 ImageOptions
        ImageOptions useImageOptions = imagePrompt.getOptions() == null ? defaultImageOptions : imagePrompt.getOptions();
        if (!(useImageOptions instanceof OpenAiImageOptions)) {
            throw new ChatException("配置信息不正确，传入的必须是 OpenAiImageOptions!");
        }
        // 转换 OpenAiImageOptions
        OpenAiImageOptions openAiImageOptions = (OpenAiImageOptions) useImageOptions;
        return openAiImageOptions;
    }

}
