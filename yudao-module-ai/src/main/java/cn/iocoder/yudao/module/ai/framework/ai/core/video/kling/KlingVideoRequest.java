package cn.iocoder.yudao.module.ai.framework.ai.core.video.kling;

import lombok.Data;

/**
 * 可灵视频生成的请求
 *
 * 1. 新版接口（模型在路径中，例如 /text-to-video/kling-2.6）：model 为路径中的模型，例如 kling-2.6、kling-2.5-turbo、kling-3.0、kling-4.0
 * 2. 旧版接口（/v1/videos/text2video、/v1/videos/image2video，model_name 传模型）：model 为 model_name，例如 kling-v2-6、kling-v2-5-turbo
 *
 * 有首帧时走图生，否则走文生；尾帧必须配首帧
 *
 * @author 芋道源码
 */
@Data
public class KlingVideoRequest {

    private String model;
    /**
     * 提示词：新版 2.5 / 2.6 最多 2500 字符；旧版最多 2500 字符
     */
    private String prompt;
    /**
     * 首帧图片：URL 或 Base64
     */
    private String firstFrame;
    /**
     * 尾帧图片：URL 或 Base64；需要首帧
     */
    private String lastFrame;
    /**
     * 分辨率（新版）：720p、1080p
     */
    private String resolution;
    /**
     * 比例：16:9、9:16、1:1（kling-4.0 另有 21:9）；图生跟随首帧
     */
    private String aspectRatio;
    /**
     * 时长（秒）：2.5 / 2.6 为 5、10
     */
    private Integer duration;
    /**
     * 是否生成原生音频（新版 settings.audio=native；旧版 sound=on）；只有 2.6 支持，且只支持 1080p、不能带尾帧
     */
    private Boolean audio;
    private String callbackUrl;
    /**
     * 自定义任务编号：账号内唯一，可用于查询
     */
    private String externalTaskId;
    /**
     * 是否同时生成带水印的结果（新版）
     */
    private Boolean watermark;

    // ========== 只用于旧版接口 ==========

    /**
     * 反向提示词
     */
    private String negativePrompt;
    /**
     * 提示词相关性：0 ~ 1
     */
    private Double cfgScale;
    /**
     * 生成模式：std、pro
     */
    private String mode;

}
