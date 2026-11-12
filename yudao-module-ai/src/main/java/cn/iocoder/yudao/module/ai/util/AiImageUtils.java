package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.openai.OpenAiImageOptions;

/**
 * AI 绘图的工具类
 *
 * @author 芋道源码
 */
public class AiImageUtils {

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
            case SILICON_FLOW:
                return SiliconFlowImageOptions.builder().model(model).width(width).height(height).build();
            case TONG_YI:
                return DashScopeImageOptions.builder().model(model).n(1).width(width).height(height).build();
            default:
                throw new IllegalArgumentException(StrUtil.format("未知平台({})", platform));
        }
    }

}
