package cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax;

import lombok.Data;

/**
 * MiniMax 海螺旧模型视频生成（v1 接口）的请求
 *
 * 生成方式由素材决定：
 * 1. 文生（T2V）：只有 prompt
 * 2. 图生（I2V）：首帧 firstFrameImage
 * 3. 首尾帧（FL2V）：尾帧 lastFrameImage（首帧可选），只有 MiniMax-Hailuo-02 支持
 * 4. 主体参考（S2V）：subjectReferenceImage（人脸），只有 S2V-01 支持
 *
 * 各模型支持的方式不同，例如 MiniMax-Hailuo-2.3-Fast 只支持图生，不能用于文生
 *
 * @author 芋道源码
 */
@Data
public class MiniMaxHailuoVideoRequest {

    /**
     * 模型：MiniMax-Hailuo-2.3、MiniMax-Hailuo-2.3-Fast、MiniMax-Hailuo-02、T2V-01-Director、T2V-01、
     * I2V-01-Director、I2V-01-live、I2V-01、S2V-01
     */
    private String model;
    /**
     * 提示词，最多 2000 字符；文生必填
     */
    private String prompt;
    /**
     * 是否自动优化提示词，默认 true
     */
    private Boolean promptOptimizer;
    /**
     * 是否缩短提示词优化耗时，只有 MiniMax-Hailuo-2.3、2.3-Fast、02 支持
     */
    private Boolean fastPretreatment;
    /**
     * 时长（秒）：6 或 10，按模型与分辨率
     */
    private Integer duration;
    /**
     * 分辨率：512P、720P、768P、1080P，按模型与时长
     */
    private String resolution;

    private String firstFrameImage;
    private String lastFrameImage;
    /**
     * 主体参考（人脸）图片，只有 S2V-01 支持
     */
    private String subjectReferenceImage;

    private String callbackUrl;

}
