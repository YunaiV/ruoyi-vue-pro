package cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 即梦数字人口播（OmniHuman）的请求：单张人物图 + 驱动音频
 *
 * 1. OmniHuman 1.5（jimeng_realman_avatar_picture_omni_v15）：CVSubmitTask；提交前需要主体识别通过
 *    （{@link JimengVisualApi#submitSubjectRecognition}），可选主体检测得到 mask 指定说话主体；音频小于 60 秒
 * 2. OmniHuman 2.0（realman_avatar_picture_omni20_cvtob）：CVSync2AsyncSubmitTask；音频小于 300 秒，720p
 *
 * @author 芋道源码
 */
@Data
public class JimengOmniHumanRequest {

    private String reqKey;
    /**
     * 人物图片地址（公网可访问）
     */
    private String imageUrl;
    /**
     * 驱动音频地址（公网可访问）
     */
    private String audioUrl;
    /**
     * 指定说话主体的 mask 图地址：只用于 1.5，来自主体检测
     */
    private List<String> maskUrls = new ArrayList<>();
    /**
     * 提示词：1.5 建议 300 字符以内；2.0 不建议超过 2000 字符
     */
    private String prompt;
    /**
     * 随机种子：只用于 1.5
     */
    private Long seed;
    /**
     * 输出分辨率：只用于 1.5，720 或 1080
     */
    private Integer outputResolution;
    /**
     * 快速模式：只用于 1.5
     */
    private Boolean peFastMode;
    /**
     * 是否启用水印：只用于 2.0，服务端默认 true
     */
    private Boolean enableWatermark;

}
