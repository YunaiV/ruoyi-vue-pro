package cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云百炼 HappyHorse 视频生成 API：文生（t2v）、首帧图生（i2v）、多图参考（r2v）
 *
 * 协议与万相相同（异步提交 + 按 task_id 查询），见 {@link DashScopeVideoTaskClient}；
 * 每个模型只支持一种生成方式，由模型名后缀 -t2v / -i2v / -r2v 决定
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/happyhorse-text-to-video-api-reference">HappyHorse 文生视频</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/happyhorse-image-to-video-api-reference">HappyHorse 图生视频-基于首帧</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/happyhorse-reference-to-video-api-reference">HappyHorse 参考生视频</a>
 * @author 芋道源码
 */
public class HappyHorseVideoApi {

    public static final String PLATFORM = "happyhorse";

    public static final List<String> T2V_MODELS = ListUtil.of("happyhorse-1.1-t2v", "happyhorse-1.0-t2v");
    public static final List<String> I2V_MODELS = ListUtil.of("happyhorse-1.1-i2v", "happyhorse-1.0-i2v");
    public static final List<String> R2V_MODELS = ListUtil.of("happyhorse-1.1-r2v", "happyhorse-1.0-r2v");

    public static final List<String> RESOLUTIONS = ListUtil.of("480P", "720P", "1080P");
    public static final List<String> RATIOS = ListUtil.of("16:9", "9:16", "1:1", "4:3", "3:4", "4:5", "5:4", "9:21", "21:9");

    private static final int MAX_PROMPT_LENGTH = 5000;
    private static final int MAX_REFERENCE_IMAGES = 9;
    private static final long MAX_SEED = 2147483647L;

    private final DashScopeVideoTaskClient client;

    /**
     * @param baseUrl 地址，为空时为北京旧域名 https://dashscope.aliyuncs.com；推荐业务空间专属域名
     *                （{@link DashScopeVideoTaskClient#workspaceBaseUrl}）
     */
    public HappyHorseVideoApi(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, null);
    }

    public HappyHorseVideoApi(String baseUrl, String apiKey, Duration timeout) {
        this.client = new DashScopeVideoTaskClient(PLATFORM, baseUrl, apiKey, timeout);
    }

    /**
     * 校验并构建 SDK 请求参数：不发请求
     */
    public static VideoSynthesisParam buildParam(HappyHorseVideoRequest request) {
        // 1. 校验
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        boolean t2v = T2V_MODELS.contains(model);
        boolean i2v = I2V_MODELS.contains(model);
        boolean r2v = R2V_MODELS.contains(model);
        AiVideoUtils.checkArgument(t2v || i2v || r2v, "HappyHorse 不支持模型({})，可选：{}、{}、{}", model,
                T2V_MODELS, I2V_MODELS, R2V_MODELS);
        if (!i2v) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        if (i2v) {
            AiVideoUtils.checkMediaUrl(request.getFirstFrame(), "首帧图片");
            AiVideoUtils.checkArgument(request.getRatio() == null, "HappyHorse 图生视频不支持 ratio，比例跟随首帧");
        } else {
            AiVideoUtils.checkArgument(StrUtil.isBlank(request.getFirstFrame()), "模型({})不支持首帧，首帧图生请使用 -i2v 模型", model);
        }
        if (r2v) {
            int count = CollUtil.size(request.getReferenceImages());
            AiVideoUtils.checkArgument(count >= 1 && count <= MAX_REFERENCE_IMAGES, "参考图需要 1 ~ {} 张", MAX_REFERENCE_IMAGES);
        } else {
            AiVideoUtils.checkArgument(CollUtil.isEmpty(request.getReferenceImages()), "模型({})不支持参考图，参考生视频请使用 -r2v 模型", model);
        }
        AiVideoUtils.checkIn(request.getResolution(), RESOLUTIONS, "分辨率");
        AiVideoUtils.checkIn(request.getRatio(), RATIOS, "比例");
        AiVideoUtils.checkRange(request.getDuration(), 3, 15, "时长");
        AiVideoUtils.checkRange(request.getSeed(), 0, MAX_SEED, "随机种子");

        // 2. 构建：使用 SDK 的 VideoSynthesisParam
        VideoSynthesisParam.VideoSynthesisParamBuilder<?, ?> builder = DashScopeVideoTaskClient.newParam(model)
                .prompt(request.getPrompt()).resolution(request.getResolution()).ratio(request.getRatio())
                .duration(request.getDuration()).watermark(request.getWatermark())
                .seed(request.getSeed() != null ? request.getSeed().intValue() : null);
        if (i2v) {
            builder.media(ListUtil.of(VideoSynthesisParam.Media.builder()
                    .type("first_frame").url(request.getFirstFrame()).build()));
        } else if (r2v) {
            List<VideoSynthesisParam.Media> media = new ArrayList<>();
            for (String url : request.getReferenceImages()) {
                media.add(VideoSynthesisParam.Media.builder()
                        .type("reference_image").url(AiVideoUtils.checkMediaUrl(url, "参考图")).build());
            }
            builder.media(media);
        }
        return builder.build();
    }

    /**
     * 校验并构建请求体 JSON（即 SDK 实际发送的 body）：不发请求
     */
    public static ObjectNode buildRequestBody(HappyHorseVideoRequest request) {
        return DashScopeVideoTaskClient.toJson(buildParam(request));
    }

    /**
     * 创建视频生成任务：只请求一次，不重试
     */
    public AiVideoTaskRef createTask(HappyHorseVideoRequest request) {
        return client.submit(buildParam(request), "提交 HappyHorse 视频任务");
    }

    /**
     * 查询视频生成任务
     */
    public AiVideoTaskResult getTask(String taskId) {
        return client.query(taskId, "查询 HappyHorse 视频任务");
    }

}
