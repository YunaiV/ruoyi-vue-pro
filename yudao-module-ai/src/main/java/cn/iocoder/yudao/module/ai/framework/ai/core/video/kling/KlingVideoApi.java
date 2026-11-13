package cn.iocoder.yudao.module.ai.framework.ai.core.video.kling;

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
 * 可灵视频生成 API
 *
 * 1. 新版接口（推荐，API Key 鉴权）：POST {baseUrl}/text-to-video/{model} 或 /image-to-video/{model}，
 *    请求体 prompt / contents + settings + options；查询统一为 GET {baseUrl}/tasks?task_ids={id}，
 *    状态 submitted、processing、succeeded、failed，结果在 outputs[type=video]
 * 2. 旧版接口（兼容，API Key 或 AccessKey/SecretKey 生成的 JWT）：POST {baseUrl}/v1/videos/text2video 或 /v1/videos/image2video，
 *    model_name 传模型；查询 GET {baseUrl}/v1/videos/{text2video|image2video}/{task_id}，状态 submitted、processing、succeed、failed。
 *    查询路径依赖创建时的路由，所以保存在 {@link AiVideoTaskRef#getRoute()} 中
 * 3. 错误：HTTP 状态码 + 响应体 {code, message, request_id}；code 为 0 才是成功
 * 4. 地址：海外 https://api-singapore.klingai.com（官方文档）；国内站的地址以控制台为准，通过 baseUrl 传入
 *
 * @see <a href="https://kling.ai/document-api/api/get-started/authentication">鉴权</a>
 * @see <a href="https://kling.ai/document-api/api/video/2-6/text-to-video">Kling 2.6 文生视频</a>
 * @see <a href="https://kling.ai/document-api/api/video/2-6/image-to-video">Kling 2.6 图生视频</a>
 * @see <a href="https://kling.ai/document-api/api/get-started/error-codes">错误码</a>
 * @author 芋道源码
 */
@Slf4j
public class KlingVideoApi {

    public static final String PLATFORM = "kling";
    public static final String DEFAULT_BASE_URL = "https://api-singapore.klingai.com";

    public static final String ROUTE_TEXT_TO_VIDEO = "text-to-video";
    public static final String ROUTE_IMAGE_TO_VIDEO = "image-to-video";
    public static final String ROUTE_LEGACY_TEXT2VIDEO = "legacy/text2video";
    public static final String ROUTE_LEGACY_IMAGE2VIDEO = "legacy/image2video";

    public static final String MODEL_2_6 = "kling-2.6";
    public static final String MODEL_2_5_TURBO = "kling-2.5-turbo";
    public static final String MODEL_4_0 = "kling-4.0";

    public static final List<String> RESOLUTIONS = ListUtil.of("720p", "1080p");
    public static final List<String> ASPECT_RATIOS = ListUtil.of("16:9", "9:16", "1:1");
    public static final List<String> ASPECT_RATIOS_WIDE = ListUtil.of("21:9", "16:9", "9:16", "1:1");
    public static final List<Integer> DURATIONS = ListUtil.of(5, 10);
    public static final List<String> LEGACY_MODES = ListUtil.of("std", "pro");

    private static final int MAX_PROMPT_LENGTH = 2500;
    private static final String RESOLUTION_1080P = "1080p";

    private final String baseUrl;
    private final KlingCredentials credentials;
    private final AiVideoHttpClient httpClient;

    public KlingVideoApi(String baseUrl, KlingCredentials credentials) {
        this(baseUrl, credentials, null);
    }

    public KlingVideoApi(String baseUrl, KlingCredentials credentials, Duration timeout) {
        if (credentials == null) {
            throw new IllegalArgumentException("可灵的凭证不能为空");
        }
        this.baseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/");
        this.credentials = credentials;
        this.httpClient = new AiVideoHttpClient(PLATFORM, timeout, AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE,
                credentials.secrets());
    }

    // ========== 新版接口 ==========

    /**
     * 校验并构建新版接口的请求体：不发请求
     *
     * @return [路由, 请求体]
     */
    public static Object[] buildRequestBody(KlingVideoRequest request) {
        // 1.1 模型、提示词、素材
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        AiVideoUtils.checkArgument(model.matches("kling-(?!v\\d)[0-9a-z.\\-]{1,32}"),
                "新版接口的模型格式应为 kling-2.6、kling-2.5-turbo 等；旧版 model_name 请使用 createLegacyTask");
        boolean i2v = StrUtil.isNotBlank(request.getFirstFrame());
        boolean hasLast = StrUtil.isNotBlank(request.getLastFrame());
        AiVideoUtils.checkArgument(!hasLast || i2v, "尾帧必须与首帧一起使用");
        if (!i2v) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        if (MODEL_2_6.equals(model) || MODEL_2_5_TURBO.equals(model)) {
            AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        }
        AiVideoUtils.checkArgument(request.getNegativePrompt() == null && request.getCfgScale() == null
                && request.getMode() == null, "新版接口不支持 negative_prompt、cfg_scale、mode，反向描述请写入 prompt");
        // 1.2 输出参数
        AiVideoUtils.checkIn(request.getResolution(), RESOLUTIONS, "分辨率");
        if (i2v) {
            AiVideoUtils.checkArgument(request.getAspectRatio() == null, "图生视频不支持 aspect_ratio，比例跟随首帧");
        } else {
            AiVideoUtils.checkIn(request.getAspectRatio(), MODEL_4_0.equals(model) ? ASPECT_RATIOS_WIDE
                    : (MODEL_2_6.equals(model) || MODEL_2_5_TURBO.equals(model) ? ASPECT_RATIOS : ASPECT_RATIOS_WIDE), "比例");
        }
        if (MODEL_2_6.equals(model) || MODEL_2_5_TURBO.equals(model)) {
            AiVideoUtils.checkIn(request.getDuration(), DURATIONS, model + " 时长");
        } else if (MODEL_4_0.equals(model)) {
            AiVideoUtils.checkRange(request.getDuration(), 3, 30, model + " 时长");
        }
        if (Boolean.TRUE.equals(request.getAudio())) {
            AiVideoUtils.checkArgument(MODEL_2_6.equals(model), "只有 kling-2.6 支持生成原生音频");
            AiVideoUtils.checkArgument(RESOLUTION_1080P.equals(request.getResolution()), "生成原生音频只支持 1080p，请显式指定");
            AiVideoUtils.checkArgument(!hasLast, "生成原生音频时不能指定尾帧");
        }
        if (hasLast && MODEL_2_6.equals(model)) {
            AiVideoUtils.checkArgument(RESOLUTION_1080P.equals(request.getResolution()), "kling-2.6 首尾帧只支持 1080p，请显式指定");
        }
        AiVideoUtils.checkArgument(request.getExternalTaskId() == null || request.getExternalTaskId().matches("[A-Za-z0-9_\\-]{1,64}"),
                "自定义任务编号格式不正确");

        // 2. 构建
        ObjectNode body = AiVideoUtils.newObject();
        if (i2v) {
            ArrayNode contents = body.putArray("contents");
            if (StrUtil.isNotBlank(request.getPrompt())) {
                contents.addObject().put("type", "prompt").put("text", request.getPrompt());
            }
            contents.addObject().put("type", "first_frame").put("url", AiVideoUtils.checkMediaUrl(request.getFirstFrame(), "首帧图片"));
            if (hasLast) {
                contents.addObject().put("type", "last_frame").put("url", AiVideoUtils.checkMediaUrl(request.getLastFrame(), "尾帧图片"));
            }
        } else {
            body.put("prompt", request.getPrompt());
        }
        ObjectNode settings = body.putObject("settings");
        if (request.getAudio() != null) {
            settings.put("audio", request.getAudio() ? "native" : "off");
        }
        if (request.getResolution() != null) {
            settings.put("resolution", request.getResolution());
        }
        if (request.getAspectRatio() != null) {
            settings.put("aspect_ratio", request.getAspectRatio());
        }
        if (request.getDuration() != null) {
            settings.put("duration", request.getDuration());
        }
        ObjectNode options = body.putObject("options");
        if (request.getCallbackUrl() != null) {
            options.put("callback_url", AiVideoUtils.checkMediaUrl(request.getCallbackUrl(), "回调地址"));
        }
        if (request.getExternalTaskId() != null) {
            options.put("external_task_id", request.getExternalTaskId());
        }
        if (request.getWatermark() != null) {
            options.putObject("watermark_info").put("enabled", request.getWatermark());
        }
        return new Object[]{i2v ? ROUTE_IMAGE_TO_VIDEO : ROUTE_TEXT_TO_VIDEO, body};
    }

    /**
     * 创建视频生成任务（新版接口，需要 API Key）：只请求一次，不重试
     */
    public AiVideoTaskRef createTask(KlingVideoRequest request) {
        AiVideoUtils.checkArgument(credentials.isApiKey(), "可灵新版接口只支持 API Key 鉴权，AccessKey/SecretKey 请使用 createLegacyTask");
        Object[] built = buildRequestBody(request);
        String route = (String) built[0];
        JsonNode root = post("/" + route + "/" + request.getModel(), (ObjectNode) built[1]);
        JsonNode data = root.get("data");
        String taskId = AiVideoUtils.text(data, "id");
        return toTaskRef(request.getModel(), route, taskId, root);
    }

    // ========== 旧版接口 ==========

    /**
     * 校验并构建旧版接口的请求体：不发请求
     *
     * @return [路由, 请求体]
     */
    public static Object[] buildLegacyRequestBody(KlingVideoRequest request) {
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        AiVideoUtils.checkArgument(model.matches("kling-v[0-9a-z\\-]{1,32}"), "旧版接口的 model_name 格式应为 kling-v2-6 等");
        boolean i2v = StrUtil.isNotBlank(request.getFirstFrame());
        AiVideoUtils.checkArgument(StrUtil.isBlank(request.getLastFrame()) || i2v, "尾帧必须与首帧一起使用");
        if (!i2v) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        AiVideoUtils.checkMaxLength(request.getNegativePrompt(), MAX_PROMPT_LENGTH, "反向提示词");
        AiVideoUtils.checkArgument(request.getCfgScale() == null || (request.getCfgScale() >= 0 && request.getCfgScale() <= 1),
                "cfg_scale 应在 [0, 1]");
        AiVideoUtils.checkIn(request.getMode(), LEGACY_MODES, "生成模式");
        AiVideoUtils.checkIn(request.getAspectRatio(), ASPECT_RATIOS, "比例");
        AiVideoUtils.checkIn(request.getDuration(), DURATIONS, "时长");
        AiVideoUtils.checkArgument(request.getResolution() == null && request.getWatermark() == null,
                "旧版接口不支持 resolution、watermark，清晰度由 mode 决定");

        ObjectNode body = AiVideoUtils.newObject();
        body.put("model_name", model);
        if (request.getPrompt() != null) {
            body.put("prompt", request.getPrompt());
        }
        if (request.getNegativePrompt() != null) {
            body.put("negative_prompt", request.getNegativePrompt());
        }
        if (request.getCfgScale() != null) {
            body.put("cfg_scale", request.getCfgScale());
        }
        if (request.getMode() != null) {
            body.put("mode", request.getMode());
        }
        if (request.getAspectRatio() != null && !i2v) {
            body.put("aspect_ratio", request.getAspectRatio());
        }
        if (request.getDuration() != null) {
            body.put("duration", String.valueOf(request.getDuration()));
        }
        if (request.getAudio() != null) {
            body.put("sound", request.getAudio() ? "on" : "off");
        }
        if (i2v) {
            body.put("image", AiVideoUtils.checkMediaUrl(request.getFirstFrame(), "首帧图片"));
            if (StrUtil.isNotBlank(request.getLastFrame())) {
                body.put("image_tail", AiVideoUtils.checkMediaUrl(request.getLastFrame(), "尾帧图片"));
            }
        }
        if (request.getCallbackUrl() != null) {
            body.put("callback_url", AiVideoUtils.checkMediaUrl(request.getCallbackUrl(), "回调地址"));
        }
        if (request.getExternalTaskId() != null) {
            AiVideoUtils.checkArgument(request.getExternalTaskId().matches("[A-Za-z0-9_\\-]{1,64}"), "自定义任务编号格式不正确");
            body.put("external_task_id", request.getExternalTaskId());
        }
        return new Object[]{i2v ? ROUTE_LEGACY_IMAGE2VIDEO : ROUTE_LEGACY_TEXT2VIDEO, body};
    }

    /**
     * 创建视频生成任务（旧版接口）：只请求一次，不重试
     */
    public AiVideoTaskRef createLegacyTask(KlingVideoRequest request) {
        Object[] built = buildLegacyRequestBody(request);
        String route = (String) built[0];
        JsonNode root = post("/v1/videos/" + StrUtil.removePrefix(route, "legacy/"), (ObjectNode) built[1]);
        String taskId = AiVideoUtils.text(root.get("data"), "task_id");
        return toTaskRef(request.getModel(), route, taskId, root);
    }

    // ========== 查询 ==========

    /**
     * 查询视频生成任务：按创建时的路由选择新版或旧版查询接口
     */
    public AiVideoTaskResult getTask(AiVideoTaskRef ref) {
        AiVideoUtils.checkArgument(ref != null, "任务引用不能为空");
        String taskId = AiVideoUtils.requireText(ref.getTaskId(), "任务编号");
        AiVideoUtils.checkArgument(taskId.matches("[A-Za-z0-9_\\-]{1,128}"), "任务编号格式不正确");
        String route = ref.getRoute();
        if (ROUTE_LEGACY_TEXT2VIDEO.equals(route) || ROUTE_LEGACY_IMAGE2VIDEO.equals(route)) {
            JsonNode root = get("/v1/videos/" + StrUtil.removePrefix(route, "legacy/") + "/" + taskId);
            return parseLegacyTask(taskId, root);
        }
        AiVideoUtils.checkArgument(ROUTE_TEXT_TO_VIDEO.equals(route) || ROUTE_IMAGE_TO_VIDEO.equals(route),
                "未知的可灵任务路由({})", route);
        // 任务编号已按字母、数字、下划线、横线校验，无需编码
        JsonNode root = get("/tasks?task_ids=" + taskId);
        JsonNode data = root.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                if (taskId.equals(AiVideoUtils.text(item, "id"))) {
                    return parseTask(taskId, item);
                }
            }
        }
        // 查询成功但列表中没有该任务：未知，不能视为成功
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(taskId);
        task.setStatus(AiVideoTaskStatus.UNKNOWN);
        task.setRequestId(AiVideoUtils.text(root, "request_id"));
        return task;
    }

    static AiVideoTaskResult parseTask(String taskId, JsonNode item) {
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(taskId);
        String status = AiVideoUtils.text(item, "status");
        task.setRawStatus(status);
        task.putMetadata("externalId", AiVideoUtils.text(item, "external_id"));
        if ("submitted".equals(status)) {
            task.setStatus(AiVideoTaskStatus.QUEUED);
        } else if ("processing".equals(status)) {
            task.setStatus(AiVideoTaskStatus.RUNNING);
        } else if ("succeeded".equals(status)) {
            JsonNode outputs = item.get("outputs");
            if (outputs != null && outputs.isArray()) {
                for (JsonNode output : outputs) {
                    if (!"video".equals(AiVideoUtils.text(output, "type")) || AiVideoUtils.text(output, "url") == null) {
                        continue;
                    }
                    AiVideoOutput video = new AiVideoOutput();
                    video.setUrl(AiVideoUtils.text(output, "url"));
                    video.setWatermarkUrl(AiVideoUtils.text(output, "watermark_url"));
                    video.setDurationSeconds(AiVideoUtils.decimal(output, "duration"));
                    video.setMimeType("video/mp4");
                    task.getVideos().add(video);
                }
            }
            task.succeedOrFailIfEmpty();
        } else if ("failed".equals(status)) {
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(item, "message")));
        } else {
            task.setStatus(AiVideoTaskStatus.UNKNOWN);
        }
        return task;
    }

    static AiVideoTaskResult parseLegacyTask(String taskId, JsonNode root) {
        JsonNode data = root.get("data");
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(taskId);
        task.setRequestId(AiVideoUtils.text(root, "request_id"));
        String status = AiVideoUtils.text(data, "task_status");
        task.setRawStatus(status);
        if ("submitted".equals(status)) {
            task.setStatus(AiVideoTaskStatus.QUEUED);
        } else if ("processing".equals(status)) {
            task.setStatus(AiVideoTaskStatus.RUNNING);
        } else if ("succeed".equals(status)) {
            JsonNode videos = data.path("task_result").get("videos");
            if (videos != null && videos.isArray()) {
                for (JsonNode item : videos) {
                    if (AiVideoUtils.text(item, "url") == null) {
                        continue;
                    }
                    AiVideoOutput video = new AiVideoOutput();
                    video.setUrl(AiVideoUtils.text(item, "url"));
                    video.setDurationSeconds(AiVideoUtils.decimal(item, "duration"));
                    video.setMimeType("video/mp4");
                    task.getVideos().add(video);
                }
            }
            task.succeedOrFailIfEmpty();
        } else if ("failed".equals(status)) {
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(data, "task_status_msg")));
        } else {
            task.setStatus(AiVideoTaskStatus.UNKNOWN);
        }
        return task;
    }

    // ========== HTTP ==========

    private AiVideoTaskRef toTaskRef(String model, String route, String taskId, JsonNode root) {
        if (taskId == null) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, 200, null,
                    AiVideoUtils.text(root, "request_id"), true,
                    "可灵视频任务提交后未返回任务编号，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[createTask][可灵视频任务已提交，model({}) route({}) taskId({})]", model, route, taskId);
        return new AiVideoTaskRef(PLATFORM, model, taskId, route, AiVideoUtils.text(root, "request_id"));
    }

    private JsonNode post(String path, ObjectNode body) {
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.POST, URI.create(baseUrl + path), headers(),
                AiVideoUtils.toJsonBytes(body), "提交可灵视频任务", true);
        return checkResult(result, true);
    }

    private JsonNode get(String pathAndQuery) {
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.GET, URI.create(baseUrl + pathAndQuery), headers(),
                null, "查询可灵视频任务", false);
        return checkResult(result, false);
    }

    private JsonNode checkResult(AiVideoHttpClient.HttpResult result, boolean submit) {
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        Integer code = AiVideoUtils.integer(root, "code");
        if (result.is2xx() && code != null && code == 0) {
            return root;
        }
        String message = StrUtil.blankToDefault(AiVideoUtils.text(root, "message"), "返回格式不正确");
        String requestId = AiVideoUtils.text(root, "request_id");
        boolean unknown = submit && (result.getStatus() >= 500 || code == null);
        AiVideoException.Kind kind = code != null ? getKind(code, result.getStatus()) : AiVideoException.kindOfHttpStatus(result.getStatus());
        log.warn("[Kling][{}失败，HTTP {}，code {}，requestId {}]", submit ? "提交" : "查询", result.getStatus(), code, requestId);
        throw new AiVideoException(PLATFORM, kind, result.getStatus(), code != null ? String.valueOf(code) : null, requestId, unknown,
                StrUtil.format("可灵视频任务{}失败（HTTP {}，code={}）：{}{}", submit ? "提交" : "查询", result.getStatus(),
                        code != null ? code : "-", AiVideoUtils.sanitize(message, credentials.secrets()),
                        unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
    }

    static AiVideoException.Kind getKind(int code, int httpStatus) {
        if (code >= 1000 && code <= 1004) {
            return AiVideoException.Kind.AUTHENTICATION;
        }
        if (code == 1101 || code == 1102) {
            return AiVideoException.Kind.INSUFFICIENT_BALANCE;
        }
        if (code == 1100 || code == 1103 || code == 1304) {
            return AiVideoException.Kind.PERMISSION;
        }
        if (code == 1200 || code == 1201) {
            return AiVideoException.Kind.INVALID_REQUEST;
        }
        if (code == 1202 || code == 1203) {
            return AiVideoException.Kind.NOT_FOUND;
        }
        if (code == 1300 || code == 1301) {
            return AiVideoException.Kind.CONTENT_REJECTED;
        }
        if (code == 1302 || code == 1303) {
            return AiVideoException.Kind.RATE_LIMITED;
        }
        if (code >= 5000 && code < 6000) {
            return AiVideoException.Kind.SERVER_ERROR;
        }
        return AiVideoException.kindOfHttpStatus(httpStatus);
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", credentials.getAuthorization());
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
