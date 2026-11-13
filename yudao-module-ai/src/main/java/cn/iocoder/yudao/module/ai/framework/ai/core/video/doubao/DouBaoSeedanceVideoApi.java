package cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoHttpClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoOutput;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskStatus;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 豆包（火山方舟 Ark）Seedance 视频生成 API
 *
 * 1. 创建：POST {baseUrl}/api/v3/contents/generations/tasks，Authorization: Bearer {ARK_API_KEY}；只发一次，返回 id
 * 2. 查询：GET {baseUrl}/api/v3/contents/generations/tasks/{id}；状态 queued、running、cancelled、succeeded、failed、expired，
 *    成功时 content.video_url 有效期 24 小时；任务编号有效期 7 天
 * 3. 错误：非 2xx，响应体 {error: {code, message, type}}
 * 4. 模型参数差异按模型 ID 中的系列识别（seedance-1-0 / 1-5 / 2-0 / 2-5）；推理接入点 ID（ep-xxx）无法识别系列，只做通用校验
 *
 * @see <a href="https://www.volcengine.com/docs/82379/1520757">创建视频生成任务</a>
 * @see <a href="https://www.volcengine.com/docs/82379/1521309">查询视频生成任务</a>
 * @author 芋道源码
 */
@Slf4j
public class DouBaoSeedanceVideoApi {

    public static final String PLATFORM = "doubao";
    public static final String DEFAULT_BASE_URL = "https://ark.cn-beijing.volces.com";
    private static final String TASKS_PATH = "/api/v3/contents/generations/tasks";

    public static final List<String> RESOLUTIONS = ListUtil.of("480p", "720p", "1080p", "4k");
    public static final List<String> RATIOS = ListUtil.of("16:9", "4:3", "1:1", "3:4", "9:16", "21:9", "adaptive");
    public static final List<String> SERVICE_TIERS = ListUtil.of("default", "flex");

    /**
     * 模型系列
     */
    public enum Series {
        V1_0, V1_5, V2_0, V2_5,
        /**
         * 推理接入点或无法识别：只做通用校验
         */
        UNKNOWN
    }

    private static final int MAX_PROMPT_LENGTH = 2000;
    private static final int MAX_REFERENCE_IMAGES = 9;
    private static final int MAX_REFERENCE_IMAGES_V25 = 30;
    private static final int MAX_REFERENCE_VIDEOS = 3;
    private static final int MAX_REFERENCE_VIDEOS_V25 = 10;
    private static final int MAX_REFERENCE_AUDIOS = 3;
    private static final int MAX_REFERENCE_AUDIOS_V25 = 10;
    private static final String[] MEDIA_SCHEMES = {"asset"};

    private final String baseUrl;
    private final String apiKey;
    private final AiVideoHttpClient httpClient;

    public DouBaoSeedanceVideoApi(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, null);
    }

    public DouBaoSeedanceVideoApi(String baseUrl, String apiKey, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("火山方舟的 API Key 不能为空");
        }
        this.baseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/");
        this.apiKey = apiKey;
        this.httpClient = new AiVideoHttpClient(PLATFORM, timeout, AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE, apiKey);
    }

    public static Series getSeries(String model) {
        if (model == null) {
            return Series.UNKNOWN;
        }
        if (model.contains("seedance-1-0")) {
            return Series.V1_0;
        }
        if (model.contains("seedance-1-5")) {
            return Series.V1_5;
        }
        if (model.contains("seedance-2-0")) {
            return Series.V2_0;
        }
        if (model.contains("seedance-2-5")) {
            return Series.V2_5;
        }
        return Series.UNKNOWN;
    }

    /**
     * 校验并构建请求体：不发请求
     */
    public static ObjectNode buildRequestBody(DouBaoSeedanceVideoRequest request) {
        // 1.1 模型、素材组合
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        Series series = getSeries(model);
        boolean known = series != Series.UNKNOWN;
        boolean hasFirst = StrUtil.isNotBlank(request.getFirstFrame());
        boolean hasLast = StrUtil.isNotBlank(request.getLastFrame());
        int imageCount = CollUtil.size(request.getReferenceImages());
        int videoCount = CollUtil.size(request.getReferenceVideos());
        int audioCount = CollUtil.size(request.getReferenceAudios());
        boolean reference = imageCount + videoCount + audioCount > 0;
        AiVideoUtils.checkArgument(!hasLast || hasFirst, "尾帧必须与首帧一起使用");
        AiVideoUtils.checkArgument(!(reference && hasFirst), "首帧 / 首尾帧与参考素材不能混用");
        if (!hasFirst && !reference) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        boolean v25 = series == Series.V2_5;
        AiVideoUtils.checkArgument(imageCount <= (v25 ? MAX_REFERENCE_IMAGES_V25 : MAX_REFERENCE_IMAGES), "参考图数量超过上限");
        AiVideoUtils.checkArgument(videoCount <= (v25 ? MAX_REFERENCE_VIDEOS_V25 : MAX_REFERENCE_VIDEOS), "参考视频数量超过上限");
        AiVideoUtils.checkArgument(audioCount <= (v25 ? MAX_REFERENCE_AUDIOS_V25 : MAX_REFERENCE_AUDIOS), "参考音频数量超过上限");
        if (known) {
            if (videoCount + audioCount > 0) {
                AiVideoUtils.checkArgument(series == Series.V2_0 || v25, "只有 Seedance 2.0 / 2.5 系列支持参考视频、参考音频");
            }
            if (audioCount > 0 && !v25) {
                AiVideoUtils.checkArgument(imageCount + videoCount > 0, "参考音频不能单独输入，需同时提供参考图或参考视频");
            }
            if (model.contains("lite-t2v")) {
                AiVideoUtils.checkArgument(!hasFirst && !reference, "模型({})只支持文生视频", model);
            }
            if (model.contains("lite-i2v")) {
                AiVideoUtils.checkArgument(hasFirst || imageCount > 0, "模型({})需要首帧或参考图", model);
            }
        }
        // 1.2 输出参数
        AiVideoUtils.checkIn(request.getResolution(), RESOLUTIONS, "分辨率");
        if (known && request.getResolution() != null) {
            if ("4k".equals(request.getResolution())) {
                AiVideoUtils.checkArgument(series == Series.V2_0 && !model.contains("fast") && !model.contains("mini"),
                        "只有 Seedance 2.0 支持 4k");
            }
            if ("1080p".equals(request.getResolution()) && series == Series.V2_0) {
                AiVideoUtils.checkArgument(!model.contains("fast") && !model.contains("mini"),
                        "Seedance 2.0 fast / mini 暂不支持 1080p");
            }
        }
        AiVideoUtils.checkIn(request.getRatio(), RATIOS, "比例");
        if (request.getDuration() != null) {
            AiVideoUtils.checkArgument(request.getDuration() == -1 || (request.getDuration() >= 2 && request.getDuration() <= 30),
                    "时长应为 -1（智能选择）或 2 ~ 30 秒");
            if (series == Series.V1_0) {
                AiVideoUtils.checkRange(request.getDuration(), 2, 12, "Seedance 1.0 时长");
            } else if (series == Series.V2_0) {
                AiVideoUtils.checkArgument(request.getDuration() == -1 || (request.getDuration() >= 4 && request.getDuration() <= 15),
                        "Seedance 2.0 时长应为 -1 或 4 ~ 15 秒");
            }
        }
        if (request.getFrames() != null) {
            AiVideoUtils.checkArgument(!known || series == Series.V1_0, "只有 Seedance 1.0 系列支持 frames");
            AiVideoUtils.checkArgument(request.getDuration() == null, "frames 与 duration 只能二选一");
            AiVideoUtils.checkArgument(request.getFrames() >= 29 && request.getFrames() <= 289
                    && (request.getFrames() - 25) % 4 == 0, "frames 应在 [29, 289] 且满足 25 + 4n");
        }
        if (request.getSeed() != null) {
            AiVideoUtils.checkArgument(!known || series == Series.V1_0, "只有 Seedance 1.0 系列支持 seed");
            AiVideoUtils.checkRange(request.getSeed(), -1, 2147483647L, "随机种子");
        }
        if (request.getCameraFixed() != null) {
            AiVideoUtils.checkArgument(!known || series == Series.V1_0, "只有 Seedance 1.0 系列支持 camera_fixed");
            AiVideoUtils.checkArgument(imageCount == 0, "参考图场景不支持 camera_fixed");
        }
        if (request.getGenerateAudio() != null && known) {
            AiVideoUtils.checkArgument(series != Series.V1_0, "Seedance 1.0 系列不支持生成音频");
        }
        AiVideoUtils.checkIn(request.getServiceTier(), SERVICE_TIERS, "服务等级");
        if ("flex".equals(request.getServiceTier()) && known) {
            AiVideoUtils.checkArgument(series == Series.V1_0, "只有 Seedance 1.0 系列支持 flex");
        }
        AiVideoUtils.checkRange(request.getExecutionExpiresAfter(), 3600, 259200, "任务超时");
        if (request.getCallbackUrl() != null) {
            AiVideoUtils.checkMediaUrl(request.getCallbackUrl(), "回调地址");
        }

        // 2. 构建
        ObjectNode body = AiVideoUtils.newObject();
        body.put("model", model);
        ArrayNode content = body.putArray("content");
        if (StrUtil.isNotBlank(request.getPrompt())) {
            content.addObject().put("type", "text").put("text", request.getPrompt());
        }
        addMedia(content, "image_url", "first_frame", request.getFirstFrame(), "首帧图片");
        addMedia(content, "image_url", "last_frame", request.getLastFrame(), "尾帧图片");
        for (String url : CollUtil.emptyIfNull(request.getReferenceImages())) {
            addMedia(content, "image_url", "reference_image", url, "参考图");
        }
        for (String url : CollUtil.emptyIfNull(request.getReferenceVideos())) {
            addMedia(content, "video_url", "reference_video", url, "参考视频");
        }
        for (String url : CollUtil.emptyIfNull(request.getReferenceAudios())) {
            addMedia(content, "audio_url", "reference_audio", url, "参考音频");
        }
        putIfNotNull(body, "resolution", request.getResolution());
        putIfNotNull(body, "ratio", request.getRatio());
        if (request.getDuration() != null) {
            body.put("duration", request.getDuration());
        }
        if (request.getFrames() != null) {
            body.put("frames", request.getFrames());
        }
        if (request.getSeed() != null) {
            body.put("seed", request.getSeed());
        }
        if (request.getCameraFixed() != null) {
            body.put("camera_fixed", request.getCameraFixed());
        }
        if (request.getWatermark() != null) {
            body.put("watermark", request.getWatermark());
        }
        if (request.getGenerateAudio() != null) {
            body.put("generate_audio", request.getGenerateAudio());
        }
        if (request.getReturnLastFrame() != null) {
            body.put("return_last_frame", request.getReturnLastFrame());
        }
        putIfNotNull(body, "callback_url", request.getCallbackUrl());
        putIfNotNull(body, "service_tier", request.getServiceTier());
        if (request.getExecutionExpiresAfter() != null) {
            body.put("execution_expires_after", request.getExecutionExpiresAfter());
        }
        return body;
    }

    private static void putIfNotNull(ObjectNode node, String field, String value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private static void addMedia(ArrayNode content, String type, String role, String url, String name) {
        if (StrUtil.isBlank(url)) {
            return;
        }
        AiVideoUtils.checkMediaUrl(url, name, MEDIA_SCHEMES);
        ObjectNode item = content.addObject();
        item.put("type", type);
        item.putObject(type).put("url", url);
        item.put("role", role);
    }

    /**
     * 创建视频生成任务：只请求一次，不重试
     */
    public AiVideoTaskRef createTask(DouBaoSeedanceVideoRequest request) {
        ObjectNode body = buildRequestBody(request);
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.POST, URI.create(baseUrl + TASKS_PATH),
                headers(), AiVideoUtils.toJsonBytes(body), "提交豆包 Seedance 视频任务", true);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        if (!result.is2xx()) {
            throw buildException(result, root, true);
        }
        String taskId = AiVideoUtils.text(root, "id");
        if (taskId == null) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null,
                    getRequestId(result), true, "豆包 Seedance 视频任务提交后未返回 id，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[createTask][豆包 Seedance 视频任务已提交，model({}) taskId({})]", request.getModel(), taskId);
        return new AiVideoTaskRef(PLATFORM, request.getModel(), taskId, null, getRequestId(result));
    }

    /**
     * 查询视频生成任务：只查询一次
     */
    public AiVideoTaskResult getTask(String taskId) {
        AiVideoUtils.requireText(taskId, "任务编号");
        AiVideoUtils.checkArgument(taskId.matches("[A-Za-z0-9_-]{1,128}"), "任务编号格式不正确");
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.GET, URI.create(baseUrl + TASKS_PATH + "/" + taskId),
                headers(), null, "查询豆包 Seedance 视频任务", false);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        if (!result.is2xx()) {
            throw buildException(result, root, false);
        }
        if (root == null || !root.isObject()) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null,
                    getRequestId(result), false, "查询豆包 Seedance 视频任务失败：返回格式不正确");
        }
        AiVideoTaskResult task = parseTask(taskId, root);
        task.setRequestId(getRequestId(result));
        return task;
    }

    static AiVideoTaskResult parseTask(String taskId, JsonNode root) {
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(StrUtil.blankToDefault(AiVideoUtils.text(root, "id"), taskId));
        String status = AiVideoUtils.text(root, "status");
        task.setRawStatus(status);
        task.putMetadata("model", AiVideoUtils.text(root, "model"));
        task.putMetadata("resolution", AiVideoUtils.text(root, "resolution"));
        task.putMetadata("ratio", AiVideoUtils.text(root, "ratio"));
        task.putMetadata("duration", AiVideoUtils.text(root, "duration"));
        task.putMetadata("frames", AiVideoUtils.text(root, "frames"));
        task.putMetadata("seed", AiVideoUtils.text(root, "seed"));
        task.putMetadata("framesPerSecond", AiVideoUtils.text(root, "framespersecond"));
        task.putMetadata("usage.completionTokens", AiVideoUtils.text(root.path("usage"), "completion_tokens"));
        if ("queued".equals(status)) {
            task.setStatus(AiVideoTaskStatus.QUEUED);
        } else if ("running".equals(status)) {
            task.setStatus(AiVideoTaskStatus.RUNNING);
        } else if ("succeeded".equals(status)) {
            JsonNode content = root.get("content");
            String url = AiVideoUtils.text(content, "video_url");
            if (url != null) {
                AiVideoOutput output = new AiVideoOutput();
                output.setUrl(url);
                output.setMimeType("video/mp4");
                output.setLastFrameUrl(AiVideoUtils.text(content, "last_frame_url"));
                output.setDurationSeconds(AiVideoUtils.decimal(root, "duration"));
                task.getVideos().add(output);
            }
            task.succeedOrFailIfEmpty();
        } else if ("failed".equals(status)) {
            task.setStatus(AiVideoTaskStatus.FAILED);
            JsonNode error = root.get("error");
            task.setErrorCode(AiVideoUtils.text(error, "code"));
            task.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(error, "message")));
        } else if ("cancelled".equals(status)) {
            task.setStatus(AiVideoTaskStatus.CANCELLED);
        } else if ("expired".equals(status)) {
            task.setStatus(AiVideoTaskStatus.EXPIRED);
        } else {
            task.setStatus(AiVideoTaskStatus.UNKNOWN);
        }
        return task;
    }

    private static String getRequestId(AiVideoHttpClient.HttpResult result) {
        return result.getHeaders() != null ? result.getHeaders().getFirst("X-Request-Id") : null;
    }

    private AiVideoException buildException(AiVideoHttpClient.HttpResult result, JsonNode root, boolean submit) {
        JsonNode error = root != null ? root.get("error") : null;
        String code = AiVideoUtils.text(error, "code");
        String message = StrUtil.blankToDefault(AiVideoUtils.text(error, "message"), "返回格式不正确");
        boolean unknown = submit && (result.getStatus() >= 500 || error == null);
        String requestId = getRequestId(result);
        log.warn("[DouBaoSeedance][{}失败，HTTP {}，code {}，requestId {}]", submit ? "提交" : "查询",
                result.getStatus(), code, requestId);
        return new AiVideoException(PLATFORM, getKind(code, result.getStatus()), result.getStatus(), code, requestId, unknown,
                StrUtil.format("豆包 Seedance 视频任务{}失败（HTTP {}，{}）：{}{}", submit ? "提交" : "查询", result.getStatus(),
                        StrUtil.blankToDefault(code, "-"), AiVideoUtils.sanitize(message, apiKey),
                        unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
    }

    static AiVideoException.Kind getKind(String code, int httpStatus) {
        if (code != null) {
            if (code.startsWith("AuthenticationError") || code.startsWith("InvalidApiKey") || code.startsWith("Unauthorized")) {
                return AiVideoException.Kind.AUTHENTICATION;
            }
            if (code.startsWith("AccountOverdue") || code.contains("InsufficientBalance") || code.contains("Arrear")) {
                return AiVideoException.Kind.INSUFFICIENT_BALANCE;
            }
            if (code.startsWith("AccessDenied") || code.startsWith("ModelNotOpen") || code.contains("NotActivated")
                    || code.startsWith("OperationDenied")) {
                return AiVideoException.Kind.PERMISSION;
            }
            if (code.contains("SensitiveContent") || code.contains("RiskDetection")) {
                return AiVideoException.Kind.CONTENT_REJECTED;
            }
            if (code.startsWith("RateLimitExceeded") || code.startsWith("QuotaExceeded") || code.contains("ServerOverloaded")) {
                return AiVideoException.Kind.RATE_LIMITED;
            }
            if (code.startsWith("ResourceNotFound") || code.startsWith("InvalidEndpointOrModel")
                    || code.startsWith("ModelNotFound")) {
                return AiVideoException.Kind.NOT_FOUND;
            }
            if (code.startsWith("InvalidParameter") || code.startsWith("MissingParameter")) {
                return AiVideoException.Kind.INVALID_REQUEST;
            }
            if (code.startsWith("InternalServiceError")) {
                return AiVideoException.Kind.SERVER_ERROR;
            }
        }
        return AiVideoException.kindOfHttpStatus(httpStatus);
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
