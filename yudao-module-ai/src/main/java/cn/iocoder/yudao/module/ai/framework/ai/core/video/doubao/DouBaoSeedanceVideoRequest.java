package cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 豆包（火山方舟）Seedance 视频生成的请求
 *
 * 生成方式由素材决定，三种图片用法不能混用：
 * 1. 文生：只有 prompt
 * 2. 首帧：firstFrame
 * 3. 首尾帧：firstFrame + lastFrame（尾帧必须配首帧）
 * 4. 参考：referenceImages（2.0 系列 1 ~ 9 张），2.0 / 2.5 系列另可带参考视频、参考音频（音频不能单独输入）
 *
 * 素材地址支持公网 URL、data:{mime};base64,{内容}、asset://{素材 ID}
 *
 * @author 芋道源码
 */
@Data
public class DouBaoSeedanceVideoRequest {

    /**
     * 模型 ID（例如 doubao-seedance-2-0-260128、doubao-seedance-1-0-pro-fast-251015），或推理接入点 ID（ep-xxx）
     */
    private String model;
    /**
     * 提示词：中文不超过 500 字，英文不超过 1000 词
     */
    private String prompt;

    private String firstFrame;
    private String lastFrame;
    private List<String> referenceImages = new ArrayList<>();
    private List<String> referenceVideos = new ArrayList<>();
    private List<String> referenceAudios = new ArrayList<>();

    /**
     * 分辨率：480p、720p、1080p、4k（按模型）
     */
    private String resolution;
    /**
     * 比例：16:9、4:3、1:1、3:4、9:16、21:9、adaptive
     */
    private String ratio;
    /**
     * 时长（秒），-1 表示由模型智能选择；与 frames 二选一
     */
    private Integer duration;
    /**
     * 帧数：只有 1.0 系列支持，[29, 289] 且满足 25 + 4n
     */
    private Integer frames;
    /**
     * 随机种子：只有 1.0 系列支持，[-1, 2147483647]
     */
    private Long seed;
    /**
     * 是否固定镜头：只有 1.0 系列支持，参考图场景不支持
     */
    private Boolean cameraFixed;
    private Boolean watermark;
    /**
     * 是否生成音频：1.5 / 2.0 / 2.5 系列支持
     */
    private Boolean generateAudio;
    /**
     * 是否返回尾帧图片
     */
    private Boolean returnLastFrame;
    private String callbackUrl;
    /**
     * 服务等级：default、flex（flex 只有 1.0 系列支持）
     */
    private String serviceTier;
    /**
     * 任务超时（秒）：[3600, 259200]
     */
    private Integer executionExpiresAfter;

}
