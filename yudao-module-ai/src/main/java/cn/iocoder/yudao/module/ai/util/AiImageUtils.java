package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ResponseFormat;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * AI 绘图的工具类
 *
 * @author 芋道源码
 */
public class AiImageUtils {

    /**
     * Seedream 是否添加水印
     */
    public static final Boolean SEEDREAM_WATERMARK = false;
    /**
     * Seedream 的提示词优化模式
     */
    public static final String SEEDREAM_OPTIMIZE_PROMPT_MODE = "standard";

    /**
     * 构建 ImageOptions：按平台创建对应的模型、尺寸等参数
     *
     * 仅 OpenAI 的非 gpt-image 模型会设置 response_format 为 b64_json；其它平台不设置返回格式，图片以 URL 还是 Base64 返回，由平台决定
     *
     * @param platform 平台
     * @param model 模型标志
     * @param width 宽度
     * @param height 高度
     * @return ImageOptions
     */
    public static ImageOptions buildImageOptions(AiPlatformEnum platform, String model, Integer width, Integer height) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                // gpt-image 系列：固定返回 b64_json，不支持 response_format 参数
                return OpenAiImageOptions.builder().model(model).width(width).height(height)
                        .responseFormat(StrUtil.startWithIgnoreCase(model, "gpt-image") ? null : "b64_json").build();
            case SILICONFLOW:
                return SiliconFlowImageOptions.builder().model(model).width(width).height(height).build();
            case ALIBABA:
                return DashScopeImageOptions.builder().model(model).n(1).width(width).height(height).build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    /**
     * 构建 AI 绘图的 ImageOptions：按平台从 {@link AiImageDO.Options} 显式映射；extra 等未登记的字段，不发送
     *
     * @param image 绘图记录
     * @return ImageOptions
     */
    public static ImageOptions buildImageOptions(AiImageDO image) {
        AiImageDO.Options options = ObjUtil.defaultIfNull(image.getOptions(), new AiImageDO.Options());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(image.getPlatform());
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case OPENAI:
                // https://platform.openai.com/docs/api-reference/images/create
                // 注意：Spring AI 的 OpenAiImageRequest 只有 model、n、quality、size、user 等字段，style 不会提交，所以不传递
                return OpenAiImageOptions.builder().model(image.getModel())
                        .width(image.getWidth()).height(image.getHeight())
                        .quality(options.getQuality())
                        .responseFormat(StrUtil.startWithIgnoreCase(image.getModel(), "gpt-image") ? null : "b64_json")
                        .build();
            case SILICONFLOW:
                return buildSiliconFlowImageOptions(image, options);
            case STABILITYAI:
                // https://platform.stability.ai/docs/api-reference#tag/SDXL-and-SD1.6/operation/textToImage
                // 未传递的参数，使用与前端一致的默认值
                return StabilityAiImageOptions.builder().model(image.getModel())
                        .width(image.getWidth()).height(image.getHeight())
                        .seed(ObjUtil.defaultIfNull(options.getSeed(), 0L))
                        .cfgScale(options.getCfgScale() != null ? options.getCfgScale().floatValue() : 7F)
                        .steps(ObjUtil.defaultIfNull(options.getSteps(), 30))
                        .sampler(StrUtil.blankToDefault(options.getSampler(), "K_DPMPP_2M"))
                        .stylePreset(options.getStylePreset())
                        .clipGuidancePreset(StrUtil.blankToDefault(options.getClipGuidancePreset(), "NONE"))
                        .build();
            case ALIBABA:
                return DashScopeImageOptions.builder().model(image.getModel()).n(1)
                        .width(image.getWidth()).height(image.getHeight())
                        .build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

    /**
     * 构建硅基流动的 ImageOptions：参考图取第 1 张；未传递的参数，沿用 SiliconFlowImageOptions 的默认值
     *
     * @see <a href="https://docs.siliconflow.cn/cn/api-reference/images/images-generations">硅基流动</a>
     */
    private static SiliconFlowImageOptions buildSiliconFlowImageOptions(AiImageDO image, AiImageDO.Options options) {
        AiImageDO.InputImage reference = CollUtil.getFirst(image.getInputImages());
        SiliconFlowImageOptions request = new SiliconFlowImageOptions().setModel(image.getModel())
                .setWidth(image.getWidth()).setHeight(image.getHeight())
                .setNegativePrompt(image.getNegativePrompt())
                .setImage(reference != null ? reference.getUrl() : null);
        if (options.getSeed() != null) {
            request.setSeed(options.getSeed());
        }
        if (options.getSteps() != null) {
            request.setNumInferenceSteps(options.getSteps());
        }
        if (options.getCfgScale() != null) {
            request.setGuidanceScale(options.getCfgScale().floatValue());
        }
        return request;
    }

    /**
     * 下载图片，转换为 Base64 的 Data URL（Midjourney Proxy 提交图片使用）
     *
     * @param url 图片地址
     * @return Data URL
     */
    public static String toBase64DataUrl(String url) {
        return "data:image/png;base64," + Base64.encode(HttpUtil.downloadBytes(url));
    }

    // ========== Seedream ==========

    /**
     * 构建 Seedream 请求：单图输出、返回 URL；参考图按顺序提交
     *
     * 注意：不传 sequential_image_generation（组图），官方默认 disabled 即单图；Seedream 5.0 pro / flash 不支持该参数，传了会报错
     */
    public static GenerateImagesRequest buildSeedreamRequest(AiImageDO image) {
        AiImageDO.SeedreamOptions options = ObjUtil.defaultIfNull(image.getSeedreamOptions(), new AiImageDO.SeedreamOptions());
        GenerateImagesRequest.OptimizePromptOptions optimizePromptOptions = new GenerateImagesRequest.OptimizePromptOptions();
        optimizePromptOptions.setMode(SEEDREAM_OPTIMIZE_PROMPT_MODE);
        GenerateImagesRequest.Builder builder = GenerateImagesRequest.builder()
                .model(image.getModel()).prompt(image.getPrompt())
                .size(options.getSize())
                .responseFormat(ResponseFormat.Url)
                .watermark(ObjUtil.defaultIfNull(options.getWatermark(), SEEDREAM_WATERMARK))
                .optimizePromptOptions(optimizePromptOptions);
        List<String> imageUrls = convertList(image.getInputImages(), AiImageDO.InputImage::getUrl);
        if (CollUtil.isNotEmpty(imageUrls)) {
            builder.image(imageUrls);
        }
        return builder.build();
    }

    // ========== Midjourney ==========

    /**
     * 拼接实际提交的提示词：各版本可用的参数，由前端控制
     *
     * 顺序：提示词 --ar 版本 --q --tile --raw --hd --c --s --seed --iw --cref --cw --sref --sw --sv --no
     */
    public static String buildMidjourneyPrompt(AiMidjourneyImagineReqVO reqVO) {
        StringBuilder prompt = new StringBuilder(StrUtil.trim(reqVO.getPrompt()));
        if (!StrUtil.containsIgnoreCase(reqVO.getPrompt(), "--ar ")) {
            prompt.append(" --ar ").append(reqVO.getAspectRatio());
        }
        prompt.append(' ').append(reqVO.getVersion());
        appendMidjourneyParam(prompt, "--q", reqVO.getQuality());
        if (Boolean.TRUE.equals(reqVO.getTile())) {
            prompt.append(" --tile");
        }
        if (Boolean.TRUE.equals(reqVO.getRaw())) {
            prompt.append(" --raw");
        }
        if (Boolean.TRUE.equals(reqVO.getHd())) {
            prompt.append(" --hd");
        }
        appendMidjourneyParam(prompt, "--c", reqVO.getChaos());
        appendMidjourneyParam(prompt, "--s", reqVO.getStylize());
        appendMidjourneyParam(prompt, "--seed", reqVO.getSeed() != null && reqVO.getSeed() >= 0 ? reqVO.getSeed() : null);
        if (CollUtil.isNotEmpty(reqVO.getReferenceImageUrls())) {
            appendMidjourneyParam(prompt, "--iw", reqVO.getImageWeight());
        }
        if (StrUtil.isNotEmpty(reqVO.getCrefUrl())) {
            prompt.append(" --cref ").append(reqVO.getCrefUrl());
            appendMidjourneyParam(prompt, "--cw", reqVO.getCharacterWeight());
        }
        if (StrUtil.isNotEmpty(reqVO.getSrefUrl())) {
            prompt.append(" --sref ").append(reqVO.getSrefUrl());
            appendMidjourneyParam(prompt, "--sw", reqVO.getStyleWeight());
            appendMidjourneyParam(prompt, "--sv", reqVO.getStyleVersion());
        }
        if (StrUtil.isNotBlank(reqVO.getNegativePrompt())) {
            prompt.append(" --no ").append(StrUtil.trim(reqVO.getNegativePrompt()));
        }
        return prompt.toString();
    }

    private static void appendMidjourneyParam(StringBuilder prompt, String name, Number value) {
        if (value != null) {
            prompt.append(' ').append(name).append(' ').append(NumberUtil.toStr(value));
        }
    }

    // TODO DONE @AI：这个还是 JDK8 兼容性哈；
    /**
     * 融图尺寸转换为比例：PORTRAIT → 2:3，LANDSCAPE → 3:2，其它 → 1:1
     */
    public static String getDimensionsRatio(String dimensions) {
        if (StrUtil.equals(dimensions, "PORTRAIT")) {
            return "2:3";
        }
        if (StrUtil.equals(dimensions, "LANDSCAPE")) {
            return "3:2";
        }
        return "1:1";
    }

    /**
     * 解析 Midjourney Proxy 的进度，例如说：45% → 45；无法解析时返回 null
     */
    public static Integer parseProgress(String progress) {
        String value = StrUtil.removeSuffix(StrUtil.trim(progress), "%");
        return NumberUtil.isInteger(value) ? Integer.valueOf(value) : null;
    }

}
