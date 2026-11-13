package cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MiniMax-H3 视频生成 API（v2 接口）
 *
 * 1. 创建：POST {baseUrl}/v2/video_generation，Bearer API Key；只发一次，返回 task_id
 * 2. 查询：GET {baseUrl}/v2/query/video_generation/{task_id}；状态 queued、running、succeeded、failed、cancelled，
 *    成功时 content.url 为限时下载地址（无需再换 file_id）
 * 3. 错误：HTTP 状态码即真实状态，响应体为 {type: error, error: {type, message, http_code}, request_id}
 * 4. 国内站与国际站的密钥不通用：国内站 https://api.minimax.cn（文档同时给出 api.minimaxi.com），国际站 https://api.minimax.io
 * 5. 注意：MiniMax-M3 是语言模型，不是视频模型；旧的 Hailuo 模型走 v1 接口，见 {@link MiniMaxHailuoVideoApi}
 *
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-v2-create">创建视频生成任务</a>
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-v2-query">查询视频生成任务</a>
 * @see <a href="https://platform.minimax.cn/docs/api-reference/video-generation-v2-create">国内站：创建视频生成任务</a>
 * @author 芋道源码
 */
@Slf4j
public class MiniMaxH3VideoApi {

    public static final String PLATFORM = "minimax-v2";
    public static final String DEFAULT_BASE_URL = "https://api.minimax.cn";
    public static final String INTERNATIONAL_BASE_URL = "https://api.minimax.io";

    public static final String MODEL_H3 = "MiniMax-H3";
    public static final String MODEL_H3_HIGHSPEED = "MiniMax-H3-Highspeed";
    public static final String MODEL_H3_LITE = "MiniMax-H3-Lite";
    public static final String MODEL_H3_MAX = "MiniMax-H3-Max";
    public static final List<String> MODELS = ListUtil.of(MODEL_H3, MODEL_H3_HIGHSPEED, MODEL_H3_LITE, MODEL_H3_MAX);

    public static final String RATIO_ADAPTIVE = "adaptive";
    public static final List<String> RATIOS = ListUtil.of(RATIO_ADAPTIVE, "21:9", "16:9", "4:3", "1:1", "3:4", "9:16");
    public static final List<String> PROMPT_EXPANSION_MODES = ListUtil.of("disabled", "balanced", "quality");

    private static final int MAX_PROMPT_LENGTH = 7000;
    private static final int MAX_REFERENCE_IMAGES = 9;
    private static final int MAX_REFERENCE_VIDEOS = 3;
    private static final int MAX_REFERENCE_AUDIOS = 3;
    private static final String[] MEDIA_SCHEMES = {"mm_file"};
    /**
     * 错误信息中的内部错误码，例如 "insufficient balance (1008)"
     */
    private static final Pattern INTERNAL_CODE_PATTERN = Pattern.compile("\\((\\d{3,5})\\)");

    private final String baseUrl;
    private final String apiKey;
    private final AiVideoHttpClient httpClient;

    public MiniMaxH3VideoApi(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, null);
    }

    public MiniMaxH3VideoApi(String baseUrl, String apiKey, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("MiniMax 的 API Key 不能为空");
        }
        this.baseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/");
        this.apiKey = apiKey;
        this.httpClient = new AiVideoHttpClient(PLATFORM, timeout, AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE, apiKey);
    }

    /**
     * 校验并构建请求体：不发请求，业务可在付费调用之前提前拒绝
     */
    public static ObjectNode buildRequestBody(MiniMaxH3VideoRequest request) {
        // 1.1 模型、提示词
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        AiVideoUtils.checkIn(model, MODELS, "MiniMax-H3 模型");
        AiVideoUtils.requireText(request.getPrompt(), "提示词");
        AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        // 1.2 分辨率、时长
        AiVideoUtils.requireText(request.getResolution(), "分辨率");
        AiVideoUtils.checkIn(request.getResolution(), getResolutions(model), model + " 分辨率");
        AiVideoUtils.checkArgument(request.getDuration() != null, "时长不能为空");
        AiVideoUtils.checkRange(request.getDuration(), MODEL_H3_MAX.equals(model) ? 5 : 4, 15, model + " 时长");
        AiVideoUtils.checkIn(request.getRatio(), RATIOS, "比例");
        // 1.3 素材组合
        boolean hasFrame = StrUtil.isNotBlank(request.getFirstFrameImage()) || StrUtil.isNotBlank(request.getLastFrameImage());
        boolean hasReference = CollUtil.isNotEmpty(request.getReferenceImages()) || CollUtil.isNotEmpty(request.getReferenceVideos())
                || CollUtil.isNotEmpty(request.getReferenceAudios());
        AiVideoUtils.checkArgument(!(hasFrame && hasReference), "首尾帧与参考素材（参考图、参考视频、参考音频）不能混用");
        AiVideoUtils.checkArgument(CollUtil.size(request.getReferenceImages()) <= MAX_REFERENCE_IMAGES, "参考图最多 {} 张", MAX_REFERENCE_IMAGES);
        AiVideoUtils.checkArgument(CollUtil.size(request.getReferenceVideos()) <= MAX_REFERENCE_VIDEOS, "参考视频最多 {} 个", MAX_REFERENCE_VIDEOS);
        AiVideoUtils.checkArgument(CollUtil.size(request.getReferenceAudios()) <= MAX_REFERENCE_AUDIOS, "参考音频最多 {} 个", MAX_REFERENCE_AUDIOS);
        if (!hasFrame && !hasReference) {
            // 文生：必须指定具体比例
            AiVideoUtils.checkArgument(StrUtil.isNotBlank(request.getRatio()) && !RATIO_ADAPTIVE.equals(request.getRatio()),
                    "文生视频必须指定比例，且不能为 adaptive");
        }
        if (hasReference && MODEL_H3_LITE.equals(model)) {
            AiVideoUtils.checkArgument(StrUtil.isNotBlank(request.getRatio()) && !RATIO_ADAPTIVE.equals(request.getRatio()),
                    "MiniMax-H3-Lite 参考生视频不支持 adaptive，必须指定比例");
        }
        if (request.getPromptExpansionMode() != null) {
            AiVideoUtils.checkArgument(MODEL_H3_MAX.equals(model), "只有 MiniMax-H3-Max 支持提示词扩写模式");
            AiVideoUtils.checkIn(request.getPromptExpansionMode(), PROMPT_EXPANSION_MODES, "提示词扩写模式");
        }
        if (request.getCallbackUrl() != null) {
            AiVideoUtils.checkMediaUrl(request.getCallbackUrl(), "回调地址");
        }

        // 2. 构建 content 数组
        ObjectNode body = AiVideoUtils.newObject();
        body.put("model", model);
        ArrayNode content = body.putArray("content");
        content.addObject().put("type", "text").put("text", request.getPrompt());
        addMedia(content, "image_url", "first_frame", request.getFirstFrameImage(), "首帧图片");
        addMedia(content, "image_url", "last_frame", request.getLastFrameImage(), "尾帧图片");
        for (String url : CollUtil.emptyIfNull(request.getReferenceImages())) {
            addMedia(content, "image_url", "reference_image", url, "参考图");
        }
        for (String url : CollUtil.emptyIfNull(request.getReferenceVideos())) {
            addMedia(content, "video_url", "reference_video", url, "参考视频");
        }
        for (String url : CollUtil.emptyIfNull(request.getReferenceAudios())) {
            addMedia(content, "audio_url", "reference_audio", url, "参考音频");
        }
        body.put("resolution", request.getResolution());
        body.put("duration", request.getDuration());
        if (request.getRatio() != null) {
            body.put("ratio", request.getRatio());
        }
        if (request.getPromptExpansionMode() != null) {
            body.putObject("extra").put("prompt_expansion_mode", request.getPromptExpansionMode());
        }
        if (request.getCallbackUrl() != null) {
            body.put("callback_url", request.getCallbackUrl());
        }
        return body;
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
     * 各模型支持的分辨率
     */
    public static List<String> getResolutions(String model) {
        if (MODEL_H3.equals(model)) {
            return ListUtil.of("768P", "2K");
        }
        if (MODELS.contains(model)) {
            return ListUtil.of("480P", "768P");
        }
        return Collections.emptyList();
    }

    /**
     * 创建视频生成任务：只请求一次，不重试
     *
     * 注意：超时、网络中断、5xx 时任务是否已创建未知（异常的 submitOutcomeUnknown 为 true），调用方不能自动重新提交
     */
    public AiVideoTaskRef createTask(MiniMaxH3VideoRequest request) {
        ObjectNode body = buildRequestBody(request);
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.POST, URI.create(baseUrl + "/v2/video_generation"),
                headers(), AiVideoUtils.toJsonBytes(body), "提交 MiniMax-H3 视频任务", true);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        if (!result.is2xx()) {
            throw buildException(result, root, true);
        }
        String taskId = root != null ? AiVideoUtils.text(root, "task_id") : null;
        if (taskId == null) {
            // 2xx 却没有任务编号：是否已创建未知
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null,
                    root != null ? AiVideoUtils.text(root, "request_id") : null, true,
                    "MiniMax-H3 视频任务提交后未返回 task_id，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[createTask][MiniMax-H3 视频任务已提交，model({}) taskId({})]", request.getModel(), taskId);
        return new AiVideoTaskRef(PLATFORM, request.getModel(), taskId, null, AiVideoUtils.text(root, "request_id"));
    }

    /**
     * 查询视频生成任务：只查询一次
     */
    public AiVideoTaskResult getTask(String taskId) {
        AiVideoUtils.requireText(taskId, "任务编号");
        AiVideoUtils.checkArgument(taskId.matches("[A-Za-z0-9_-]{1,128}"), "任务编号格式不正确");
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.GET,
                URI.create(baseUrl + "/v2/query/video_generation/" + taskId), headers(), null, "查询 MiniMax-H3 视频任务", false);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        if (!result.is2xx()) {
            throw buildException(result, root, false);
        }
        if (root == null || !root.isObject()) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null, null, false,
                    "查询 MiniMax-H3 视频任务失败：返回格式不正确");
        }
        // response_format=task 时字段在 task 下；flat 时在顶层
        JsonNode task = root.has("task") ? root.get("task") : root;
        return parseTask(taskId, task);
    }

    static AiVideoTaskResult parseTask(String taskId, JsonNode task) {
        AiVideoTaskResult result = new AiVideoTaskResult();
        result.setTaskId(StrUtil.blankToDefault(AiVideoUtils.text(task, "id"), taskId));
        String status = AiVideoUtils.text(task, "status");
        result.setRawStatus(status);
        result.putMetadata("model", AiVideoUtils.text(task, "model"));
        result.putMetadata("resolution", AiVideoUtils.text(task, "resolution"));
        result.putMetadata("duration", AiVideoUtils.text(task, "duration"));
        result.putMetadata("ratio", AiVideoUtils.text(task, "ratio"));
        result.putMetadata("taskType", AiVideoUtils.text(task, "task_type"));
        JsonNode usage = task.get("usage");
        if (usage != null && usage.isObject()) {
            result.putMetadata("usage.totalSeconds", AiVideoUtils.text(usage, "total_seconds"));
            result.putMetadata("usage.outputSeconds", AiVideoUtils.text(usage, "output_seconds"));
            result.putMetadata("usage.totalTokens", AiVideoUtils.text(usage, "total_tokens"));
        }
        if ("queued".equals(status)) {
            result.setStatus(AiVideoTaskStatus.QUEUED);
        } else if ("running".equals(status)) {
            result.setStatus(AiVideoTaskStatus.RUNNING);
        } else if ("succeeded".equals(status)) {
            JsonNode content = task.get("content");
            String url = AiVideoUtils.text(content, "url");
            if (url != null) {
                AiVideoOutput output = new AiVideoOutput();
                output.setUrl(url);
                output.setMimeType("video/mp4");
                output.setDurationSeconds(AiVideoUtils.decimal(task, "duration"));
                result.getVideos().add(output);
            }
            result.succeedOrFailIfEmpty();
        } else if ("failed".equals(status)) {
            result.setStatus(AiVideoTaskStatus.FAILED);
            JsonNode error = task.get("error");
            result.setErrorCode(AiVideoUtils.text(error, "code"));
            result.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(error, "message")));
        } else if ("cancelled".equals(status)) {
            result.setStatus(AiVideoTaskStatus.CANCELLED);
        } else if ("expired".equals(status)) {
            result.setStatus(AiVideoTaskStatus.EXPIRED);
        } else {
            result.setStatus(AiVideoTaskStatus.UNKNOWN);
        }
        return result;
    }

    private AiVideoException buildException(AiVideoHttpClient.HttpResult result, JsonNode root, boolean submit) {
        JsonNode error = root != null ? root.get("error") : null;
        String type = AiVideoUtils.text(error, "type");
        String message = StrUtil.blankToDefault(AiVideoUtils.text(error, "message"), "返回格式不正确");
        String requestId = AiVideoUtils.text(root, "request_id");
        Matcher matcher = INTERNAL_CODE_PATTERN.matcher(message);
        String code = matcher.find() ? matcher.group(1) : type;
        AiVideoException.Kind kind = getKind(type, code, result.getStatus(), message);
        // 5xx、格式不正确的响应：创建任务是否成功未知
        boolean unknown = submit && (result.getStatus() >= 500 || root == null);
        log.warn("[MiniMaxH3][{}失败，HTTP {}，type {}，code {}，requestId {}]", submit ? "提交" : "查询",
                result.getStatus(), type, code, requestId);
        return new AiVideoException(PLATFORM, kind, result.getStatus(), code, requestId, unknown,
                StrUtil.format("MiniMax-H3 视频任务{}失败（HTTP {}，{}）：{}{}", submit ? "提交" : "查询", result.getStatus(),
                        StrUtil.blankToDefault(code, "-"), AiVideoUtils.sanitize(message, apiKey),
                        unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
    }

    static AiVideoException.Kind getKind(String type, String code, int httpStatus, String message) {
        if ("2049".equals(code) || "1004".equals(code) || "authorized_error".equals(type)) {
            return AiVideoException.Kind.AUTHENTICATION;
        }
        if ("1008".equals(code) || "insufficient_balance_error".equals(type)) {
            return AiVideoException.Kind.INSUFFICIENT_BALANCE;
        }
        if ("1026".equals(code) || "1027".equals(code) || "unprocessable_entity_error".equals(type)) {
            return AiVideoException.Kind.CONTENT_REJECTED;
        }
        if ("1002".equals(code) || "rate_limit_error".equals(type)) {
            return AiVideoException.Kind.RATE_LIMITED;
        }
        if ("2013".equals(code) && StrUtil.containsIgnoreCase(message, "does not exist")) {
            return AiVideoException.Kind.NOT_FOUND;
        }
        if ("server_error".equals(type) || "overloaded_error".equals(type)
                || "1000".equals(code) || "1001".equals(code) || "1013".equals(code)) {
            return AiVideoException.Kind.SERVER_ERROR;
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
