package cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿里云百炼通义万相视频生成 API：文生、首帧图生（2.1 ~ 2.6）、2.7 通用图生（首尾帧、驱动音频、视频续写）
 *
 * 协议见 {@link DashScopeVideoTaskClient}（基于官方 SDK）；参数结构按模型系列区分，见 {@link TongYiWanVideoRequest}，
 * 校验后转换为 SDK 的 {@link VideoSynthesisParam}
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/text-to-video-api-reference">万相文生视频（2.7）</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/legacy-wan-text-to-video-api-reference">万相文生视频（2.1 ~ 2.6）</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/image-to-video-api-reference">万相图生视频-基于首帧（2.1 ~ 2.6）</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/image-to-video-general-api-reference">万相图生视频-通用（2.7）</a>
 * @author 芋道源码
 */
public class TongYiWanVideoApi {

    public static final String PLATFORM = "tongyi-wan";

    /**
     * 参数结构
     */
    public enum Family {
        /**
         * 2.7 文生：resolution + ratio
         */
        T2V_V27,
        /**
         * 2.7 图生：input.media
         */
        I2V_V27,
        /**
         * 2.6 及更早文生：size
         */
        T2V_LEGACY,
        /**
         * 2.6 及更早首帧图生：img_url + resolution
         */
        I2V_LEGACY
    }

    public static final List<String> LEGACY_T2V_MODELS = ListUtil.of("wan2.6-t2v", "wan2.5-t2v-preview",
            "wan2.2-t2v-plus", "wanx2.1-t2v-turbo", "wanx2.1-t2v-plus");
    /**
     * 2.6 及更早首帧图生：模型 → 可选分辨率（第一个为默认）
     */
    public static final Map<String, List<String>> LEGACY_I2V_RESOLUTIONS = new LinkedHashMap<>();
    /**
     * 时长：模型 → 可选值；为 null 的模型只做范围校验
     */
    private static final Map<String, List<Integer>> LEGACY_DURATIONS = new LinkedHashMap<>();

    static {
        LEGACY_I2V_RESOLUTIONS.put("wan2.6-i2v-flash", ListUtil.of("1080P", "720P"));
        LEGACY_I2V_RESOLUTIONS.put("wan2.6-i2v", ListUtil.of("1080P", "720P"));
        LEGACY_I2V_RESOLUTIONS.put("wan2.5-i2v-preview", ListUtil.of("1080P", "480P", "720P"));
        LEGACY_I2V_RESOLUTIONS.put("wan2.2-i2v-flash", ListUtil.of("720P", "480P", "1080P"));
        LEGACY_I2V_RESOLUTIONS.put("wan2.2-i2v-plus", ListUtil.of("1080P", "480P"));
        LEGACY_I2V_RESOLUTIONS.put("wanx2.1-i2v-turbo", ListUtil.of("720P", "480P"));
        LEGACY_I2V_RESOLUTIONS.put("wanx2.1-i2v-plus", ListUtil.of("720P"));
        LEGACY_DURATIONS.put("wan2.5-t2v-preview", ListUtil.of(5, 10));
        LEGACY_DURATIONS.put("wan2.2-t2v-plus", ListUtil.of(5));
        LEGACY_DURATIONS.put("wan2.5-i2v-preview", ListUtil.of(5, 10));
        LEGACY_DURATIONS.put("wan2.2-i2v-flash", ListUtil.of(5));
        LEGACY_DURATIONS.put("wan2.2-i2v-plus", ListUtil.of(5));
        LEGACY_DURATIONS.put("wanx2.1-i2v-turbo", ListUtil.of(3, 4, 5));
        LEGACY_DURATIONS.put("wanx2.1-i2v-plus", ListUtil.of(5));
    }

    public static final List<String> V27_RESOLUTIONS = ListUtil.of("720P", "1080P");
    public static final List<String> V27_RATIOS = ListUtil.of("16:9", "9:16", "1:1", "4:3", "3:4");
    public static final List<String> SHOT_TYPES = ListUtil.of("single", "multi");

    private static final int MAX_PROMPT_LENGTH_V27 = 5000;
    private static final int MAX_PROMPT_LENGTH_LEGACY = 1500;
    private static final int MAX_NEGATIVE_PROMPT_LENGTH = 500;
    private static final long MAX_SEED = 2147483647L;

    private final DashScopeVideoTaskClient client;

    public TongYiWanVideoApi(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, null);
    }

    public TongYiWanVideoApi(String baseUrl, String apiKey, Duration timeout) {
        this.client = new DashScopeVideoTaskClient(PLATFORM, baseUrl, apiKey, timeout);
    }

    /**
     * 按模型判断参数结构；不支持的模型抛出 {@link IllegalArgumentException}
     */
    public static Family getFamily(String model) {
        AiVideoUtils.requireText(model, "模型");
        if (model.startsWith("wan2.7-t2v")) {
            return Family.T2V_V27;
        }
        if (model.startsWith("wan2.7-i2v")) {
            return Family.I2V_V27;
        }
        if (LEGACY_T2V_MODELS.contains(model)) {
            return Family.T2V_LEGACY;
        }
        if (LEGACY_I2V_RESOLUTIONS.containsKey(model)) {
            return Family.I2V_LEGACY;
        }
        throw new IllegalArgumentException(StrUtil.format("通义万相视频不支持模型({})，可选：wan2.7-t2v*、wan2.7-i2v*、{}、{}",
                model, LEGACY_T2V_MODELS, LEGACY_I2V_RESOLUTIONS.keySet()));
    }

    /**
     * 校验并构建 SDK 请求参数（不含 API Key）：不发请求
     */
    public static VideoSynthesisParam buildParam(TongYiWanVideoRequest request) {
        String model = request.getModel();
        Family family = getFamily(model);
        boolean v27 = family == Family.T2V_V27 || family == Family.I2V_V27;
        boolean t2v = family == Family.T2V_V27 || family == Family.T2V_LEGACY;
        boolean wan26 = model.startsWith("wan2.6-");
        boolean wan25 = model.startsWith("wan2.5-");

        // 1.1 提示词、音频
        if (t2v) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        AiVideoUtils.checkMaxLength(request.getPrompt(), v27 ? MAX_PROMPT_LENGTH_V27 : MAX_PROMPT_LENGTH_LEGACY, "提示词");
        AiVideoUtils.checkMaxLength(request.getNegativePrompt(), MAX_NEGATIVE_PROMPT_LENGTH, "反向提示词");
        if (request.getAudioUrl() != null) {
            AiVideoUtils.checkArgument(family == Family.T2V_V27 || wan26 || wan25, "模型({})不支持 audio_url", model);
            AiVideoUtils.checkMediaUrl(request.getAudioUrl(), "音频地址", "oss");
        }
        // 1.2 尺寸、分辨率、比例
        if (family == Family.T2V_LEGACY) {
            AiVideoUtils.checkArgument(request.getSize() == null || request.getSize().matches("\\d{3,4}\\*\\d{3,4}"),
                    "尺寸格式应为 宽*高，例如 1280*720");
            AiVideoUtils.checkArgument(request.getResolution() == null && request.getRatio() == null,
                    "模型({})用 size 指定尺寸，不支持 resolution、ratio", model);
        } else {
            AiVideoUtils.checkArgument(request.getSize() == null, "模型({})不支持 size，请使用 resolution", model);
            AiVideoUtils.checkIn(request.getResolution(), v27 ? V27_RESOLUTIONS : LEGACY_I2V_RESOLUTIONS.get(model),
                    model + " 分辨率");
            if (family == Family.T2V_V27) {
                AiVideoUtils.checkIn(request.getRatio(), V27_RATIOS, "比例");
            } else {
                AiVideoUtils.checkArgument(request.getRatio() == null, "图生视频不支持 ratio，比例跟随输入素材");
            }
        }
        // 1.3 时长
        if (v27 || "wan2.6-t2v".equals(model) || model.startsWith("wan2.6-i2v")) {
            AiVideoUtils.checkRange(request.getDuration(), 2, 15, "时长");
        } else if (request.getDuration() != null && LEGACY_DURATIONS.containsKey(model)) {
            AiVideoUtils.checkIn(request.getDuration(), LEGACY_DURATIONS.get(model), model + " 时长");
        } else {
            AiVideoUtils.checkRange(request.getDuration(), 1, 15, "时长");
        }
        // 1.4 其它参数
        AiVideoUtils.checkRange(request.getSeed(), 0, MAX_SEED, "随机种子");
        if (request.getShotType() != null) {
            AiVideoUtils.checkArgument(wan26, "只有万相 2.6 系列支持 shot_type");
            AiVideoUtils.checkIn(request.getShotType(), SHOT_TYPES, "镜头类型");
            AiVideoUtils.checkArgument(!Boolean.FALSE.equals(request.getPromptExtend()), "shot_type 需要开启 prompt_extend");
        }
        if (request.getAudio() != null) {
            AiVideoUtils.checkArgument("wan2.6-i2v-flash".equals(model), "只有 wan2.6-i2v-flash 支持 audio 参数");
        }
        // 1.5 素材
        if (t2v) {
            AiVideoUtils.checkArgument(StrUtil.isAllBlank(request.getFirstFrame(), request.getLastFrame(),
                    request.getDrivingAudio(), request.getFirstClip()), "文生视频不支持首帧、尾帧、驱动音频、首段视频");
        } else if (family == Family.I2V_LEGACY) {
            AiVideoUtils.checkMediaUrl(request.getFirstFrame(), "首帧图片", "oss");
            AiVideoUtils.checkArgument(StrUtil.isAllBlank(request.getLastFrame(), request.getDrivingAudio(), request.getFirstClip()),
                    "模型({})只支持首帧图生，首尾帧、驱动音频、视频续写请使用 wan2.7-i2v", model);
        } else {
            boolean first = StrUtil.isNotBlank(request.getFirstFrame());
            boolean clip = StrUtil.isNotBlank(request.getFirstClip());
            AiVideoUtils.checkArgument(first ^ clip, "万相 2.7 图生需要首帧或首段视频（二选一）");
            AiVideoUtils.checkArgument(!(clip && StrUtil.isNotBlank(request.getDrivingAudio())), "首段视频不能与驱动音频同时使用");
        }

        // 2. 构建 SDK 请求参数：未设置的字段不发送
        VideoSynthesisParam.VideoSynthesisParamBuilder<?, ?> builder = DashScopeVideoTaskClient.newParam(model)
                .prompt(request.getPrompt()).negativePrompt(request.getNegativePrompt()).audioUrl(request.getAudioUrl())
                .size(request.getSize()).resolution(request.getResolution()).ratio(request.getRatio())
                .duration(request.getDuration()).promptExtend(request.getPromptExtend()).watermark(request.getWatermark())
                .seed(request.getSeed() != null ? request.getSeed().intValue() : null)
                .shotType(request.getShotType()).audio(request.getAudio());
        if (family == Family.I2V_LEGACY) {
            builder.imgUrl(request.getFirstFrame());
        } else if (family == Family.I2V_V27) {
            List<VideoSynthesisParam.Media> media = new ArrayList<>();
            addMedia(media, "first_frame", request.getFirstFrame(), "首帧图片");
            addMedia(media, "last_frame", request.getLastFrame(), "尾帧图片");
            addMedia(media, "driving_audio", request.getDrivingAudio(), "驱动音频");
            addMedia(media, "first_clip", request.getFirstClip(), "首段视频");
            builder.media(media);
        }
        DashScopeVideoTaskClient.resolveOssResources(builder, request.getAudioUrl(), request.getFirstFrame(),
                request.getLastFrame(), request.getDrivingAudio(), request.getFirstClip());
        return builder.build();
    }

    /**
     * 校验并返回 SDK 实际发送的请求体：不发请求
     */
    public static ObjectNode buildRequestBody(TongYiWanVideoRequest request) {
        return DashScopeVideoTaskClient.toJson(buildParam(request));
    }

    private static void addMedia(List<VideoSynthesisParam.Media> media, String type, String url, String name) {
        if (StrUtil.isNotBlank(url)) {
            media.add(VideoSynthesisParam.Media.builder().type(type).url(AiVideoUtils.checkMediaUrl(url, name, "oss")).build());
        }
    }

    /**
     * 创建视频生成任务：只请求一次，不重试
     */
    public AiVideoTaskRef createTask(TongYiWanVideoRequest request) {
        return client.submit(buildParam(request), "提交通义万相视频任务");
    }

    /**
     * 查询视频生成任务
     */
    public AiVideoTaskResult getTask(String taskId) {
        return client.query(taskId, "查询通义万相视频任务");
    }

}
