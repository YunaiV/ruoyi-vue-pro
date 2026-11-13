package cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi;

import lombok.Data;

/**
 * 阿里云百炼通义万相视频生成的请求
 *
 * 不同系列的参数结构不同，由模型决定：
 * 1. 万相 2.7 文生（wan2.7-t2v*）：resolution（720P、1080P）+ ratio
 * 2. 万相 2.7 图生（wan2.7-i2v*）：input.media，组合为 首帧 / 首帧+驱动音频 / 首尾帧 / 首尾帧+驱动音频 / 首段视频 / 首段视频+尾帧
 * 3. 万相 2.6 及更早文生（wan2.6-t2v 等）：size（宽*高，例如 1280*720）
 * 4. 万相 2.6 及更早首帧图生（wan2.6-i2v、wan2.6-i2v-flash、wan2.5-i2v-preview 等）：input.img_url + resolution
 *
 * @author 芋道源码
 */
@Data
public class TongYiWanVideoRequest {

    private String model;
    /**
     * 提示词：2.7 最多 5000 字符，2.6 / 2.5 最多 1500 字符；文生必填
     */
    private String prompt;
    /**
     * 反向提示词，最多 500 字符
     */
    private String negativePrompt;
    /**
     * 音频地址（wav / mp3）：2.7 文生、2.6 / 2.5 系列支持
     */
    private String audioUrl;

    /**
     * 输出尺寸，宽*高：只用于 2.6 及更早的文生
     */
    private String size;
    /**
     * 分辨率档位：720P、1080P（480P 视模型）
     */
    private String resolution;
    /**
     * 比例：只用于 2.7 文生（16:9、9:16、1:1、4:3、3:4）
     */
    private String ratio;
    /**
     * 时长（秒），按模型
     */
    private Integer duration;
    /**
     * 是否开启提示词智能改写，服务端默认 true
     */
    private Boolean promptExtend;
    /**
     * 是否添加「AI生成」水印，服务端默认 false
     */
    private Boolean watermark;
    /**
     * 随机种子：0 ~ 2147483647
     */
    private Long seed;
    /**
     * 镜头类型：single、multi；只有 2.6 系列支持，且需 promptExtend 为 true
     */
    private String shotType;
    /**
     * 是否生成声音：只有 wan2.6-i2v-flash 支持
     */
    private Boolean audio;

    /**
     * 首帧图片：2.6 及更早图生为 img_url；2.7 图生为 media.first_frame
     */
    private String firstFrame;
    /**
     * 尾帧图片：只用于 2.7 图生
     */
    private String lastFrame;
    /**
     * 驱动音频：只用于 2.7 图生
     */
    private String drivingAudio;
    /**
     * 首段视频（视频续写）：只用于 2.7 图生
     */
    private String firstClip;

}
