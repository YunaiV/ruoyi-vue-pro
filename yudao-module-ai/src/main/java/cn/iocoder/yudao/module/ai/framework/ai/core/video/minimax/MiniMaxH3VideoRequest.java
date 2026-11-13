package cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MiniMax-H3 视频生成（v2 接口）的请求
 *
 * 生成方式由素材决定：
 * 1. 文生：只有 prompt，ratio 必填且不能为 adaptive
 * 2. 图生：首帧、尾帧（可单独尾帧，或首尾帧），ratio 跟随图片
 * 3. 参考生：参考图（≤9）、参考视频（≤3）、参考音频（≤3）的任意组合；不能与首尾帧混用
 *
 * 素材地址支持公网 URL、mm_file://{file_id}、data:{mime};base64,{内容}
 *
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-v2-create">创建视频生成任务</a>
 * @author 芋道源码
 */
@Data
public class MiniMaxH3VideoRequest {

    /**
     * 模型：MiniMax-H3、MiniMax-H3-Highspeed、MiniMax-H3-Lite、MiniMax-H3-Max
     */
    private String model;
    /**
     * 提示词，必填，最多 7000 字符
     */
    private String prompt;
    /**
     * 分辨率，必填：480P、768P、2K（按模型可选）
     */
    private String resolution;
    /**
     * 时长（秒），必填：4 ~ 15（MiniMax-H3-Max 为 5 ~ 15）
     */
    private Integer duration;
    /**
     * 比例：adaptive、21:9、16:9、4:3、1:1、3:4、9:16
     */
    private String ratio;

    private String firstFrameImage;
    private String lastFrameImage;
    private List<String> referenceImages = new ArrayList<>();
    private List<String> referenceVideos = new ArrayList<>();
    private List<String> referenceAudios = new ArrayList<>();

    /**
     * 提示词扩写模式：disabled、balanced、quality；只有 MiniMax-H3-Max 支持
     */
    private String promptExpansionMode;
    /**
     * 回调地址：MiniMax 会先发送 challenge 校验
     */
    private String callbackUrl;

}
