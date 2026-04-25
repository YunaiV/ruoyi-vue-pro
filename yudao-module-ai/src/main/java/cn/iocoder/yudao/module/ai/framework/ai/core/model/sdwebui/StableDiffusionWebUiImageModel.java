package cn.iocoder.yudao.module.ai.framework.ai.core.model.sdwebui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Stable Diffusion WebUI (AUTOMATIC1111) {@link ImageModel} 实现类
 *
 * <p>将 Spring AI 的 {@link ImagePrompt} 映射为 SD WebUI 的
 * {@code POST /sdapi/v1/txt2img} 请求，并将 base64 图片结果
 * 转换为标准 {@link ImageResponse} 返回。</p>
 *
 * <p>典型使用场景：用户在 RunPod / 本机以 Docker 启动 AUTOMATIC1111，
 * 本系统通过该 {@link ImageModel} 统一调用，无需了解底层 HTTP 细节。</p>
 *
 * <p>配置示例（application-local.yaml）：
 * <pre>{@code
 * yudao:
 *   ai:
 *     stable-diffusion-web-ui:
 *       enable: true
 *       base-url: http://103.196.86.126:15112
 *       # api-key 可选，仅在 --api-auth user:password 时填写
 * }</pre>
 * </p>
 *
 * @author deepay
 */
@Slf4j
@RequiredArgsConstructor
public class StableDiffusionWebUiImageModel implements ImageModel {

    private final StableDiffusionWebUiApi sdWebUiApi;

    /**
     * 默认 SD WebUI 服务地址（本地启动时）
     */
    public static final String DEFAULT_BASE_URL = "http://localhost:7860";

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {
        Assert.notNull(imagePrompt, "imagePrompt must not be null");
        Assert.notEmpty(imagePrompt.getInstructions(), "imagePrompt must have at least one instruction");

        // 1. 提取 prompt（取第一条指令的文本）
        String prompt = imagePrompt.getInstructions().get(0).getText();

        // 2. 提取选项（运行时 options 优先于构造时默认 options）
        StableDiffusionWebUiImageOptions options = mergeOptions(imagePrompt.getOptions());

        // 3. 构建 SD WebUI 请求
        StableDiffusionWebUiApi.Txt2ImgRequest request = buildRequest(prompt, options);

        // 4. 调用 API
        log.debug("[StableDiffusionWebUiImageModel][txt2img] prompt={}, size={}x{}",
                prompt, request.getWidth(), request.getHeight());
        ResponseEntity<StableDiffusionWebUiApi.Txt2ImgResponse> entity = sdWebUiApi.txt2img(request);

        // 5. 转换响应
        return convertResponse(entity.getBody());
    }

    private StableDiffusionWebUiImageOptions mergeOptions(ImageOptions runtimeOptions) {
        if (runtimeOptions instanceof StableDiffusionWebUiImageOptions) {
            return (StableDiffusionWebUiImageOptions) runtimeOptions;
        }
        // 将通用 ImageOptions 中的 width/height 应用到默认选项
        StableDiffusionWebUiImageOptions.StableDiffusionWebUiImageOptionsBuilder builder =
                StableDiffusionWebUiImageOptions.builder();
        if (runtimeOptions != null) {
            if (runtimeOptions.getWidth() != null) {
                builder.width(runtimeOptions.getWidth());
            }
            if (runtimeOptions.getHeight() != null) {
                builder.height(runtimeOptions.getHeight());
            }
        }
        return builder.build();
    }

    private StableDiffusionWebUiApi.Txt2ImgRequest buildRequest(String prompt,
                                                                  StableDiffusionWebUiImageOptions options) {
        return StableDiffusionWebUiApi.Txt2ImgRequest.builder()
                .prompt(prompt)
                .negativePrompt(options.getNegativePrompt())
                .width(options.getWidth())
                .height(options.getHeight())
                .steps(options.getSteps())
                .cfgScale(options.getCfgScale())
                .samplerName(options.getSamplerName())
                .seed(options.getSeed())
                .batchSize(options.getBatchSize())
                .overrideSettings(options.getOverrideSettings())
                .build();
    }

    private ImageResponse convertResponse(StableDiffusionWebUiApi.Txt2ImgResponse body) {
        if (body == null || CollectionUtils.isEmpty(body.getImages())) {
            log.warn("[StableDiffusionWebUiImageModel][txt2img] 服务返回空图片列表");
            return new ImageResponse(List.of());
        }

        List<ImageGeneration> generations = body.getImages().stream()
                .map(b64 -> new ImageGeneration(
                        new Image(null, b64),  // url=null，b64Json=base64 字符串
                        new ImageGenerationMetadata() {}))
                .toList();

        return new ImageResponse(generations);
    }

}
