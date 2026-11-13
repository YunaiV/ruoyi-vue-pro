package cn.iocoder.yudao.module.ai.framework.ai.core.video.veo;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Google Veo 视频生成 API：Vertex AI（Gemini Enterprise Agent Platform）正式（GA）接口
 *
 * 1. 创建：POST https://{location}-aiplatform.googleapis.com/v1/projects/{project}/locations/{location}/publishers/google/models/{model}:predictLongRunning，
 *    Authorization: Bearer {OAuth 访问令牌（ADC / 服务账号）}；返回长时操作 name
 * 2. 查询：POST 同一模型的 :fetchPredictOperation，请求体 {"operationName": name}；
 *    done=false 表示生成中；done=true 时 error 或 response.videos[]（gcsUri 或 bytesBase64Encoded），raiMediaFilteredCount 为被过滤数量
 * 3. 访问令牌由调用方提供（例如 google-auth-library 的 ADC，或 gcloud auth print-access-token），本类不读取凭证文件
 * 4. 不使用 Gemini API（generativelanguage.googleapis.com）：其 Veo 3.1 Preview 已于 2026-10-22 停止服务；
 *    也不使用任何第三方代理
 * 5. gcsUri 结果需要 GCP 凭证才能下载，本类不负责下载，避免把令牌转发到其它地址
 *
 * @see <a href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/models/veo/3-1-generate">Veo 3.1 模型</a>
 * @see <a href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/video/use-reference-images-to-guide-video-generation">参考图生成视频（REST）</a>
 * @see <a href="https://ai.google.dev/gemini-api/docs/video">Gemini API 视频生成（Veo 3.1 Preview 已停止）</a>
 * @author 芋道源码
 */
@Slf4j
public class VertexVeoVideoApi {

    public static final String PLATFORM = "vertex-veo";
    public static final String DEFAULT_LOCATION = "us-central1";

    public static final String MODEL_3_1 = "veo-3.1-generate-001";
    public static final String MODEL_3_1_FAST = "veo-3.1-fast-generate-001";
    public static final String MODEL_3_1_LITE = "veo-3.1-lite-generate-001";

    public static final List<String> ASPECT_RATIOS = ListUtil.of("16:9", "9:16");
    public static final List<String> RESOLUTIONS = ListUtil.of("720p", "1080p", "4k");
    public static final List<Integer> DURATIONS_3_1 = ListUtil.of(4, 6, 8);
    public static final List<String> IMAGE_MIME_TYPES = ListUtil.of("image/png", "image/jpeg");
    public static final List<String> PERSON_GENERATIONS = ListUtil.of("allow_adult", "dont_allow", "allow_all");

    private static final int MAX_REFERENCE_IMAGES = 3;
    private static final long MAX_SEED = 4294967295L;
    /**
     * 查询结果可能内联 base64 视频，默认响应上限
     */
    public static final int DEFAULT_MAX_RESPONSE_SIZE = 128 * 1024 * 1024;
    private static final Pattern MODEL_PATTERN = Pattern.compile("veo-[0-9a-z.\\-]{1,64}");
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9\\-]{0,62}");

    private final String projectId;
    private final String location;
    private final String baseUrl;
    private final Supplier<String> accessTokenSupplier;
    private final AiVideoHttpClient httpClient;

    /**
     * @param projectId GCP 项目 ID
     * @param location 地域，为空时为 us-central1（Veo 3.1 文档列出的地域）
     * @param accessTokenSupplier OAuth 访问令牌提供者，每次请求调用一次
     * @param baseUrl 地址，为空时按地域生成；测试时可指向本地服务
     */
    public VertexVeoVideoApi(String projectId, String location, Supplier<String> accessTokenSupplier, String baseUrl,
                             Duration timeout, int maxResponseSize) {
        AiVideoUtils.checkArgument(StrUtil.isNotBlank(projectId) && ID_PATTERN.matcher(projectId).matches(), "GCP 项目 ID 格式不正确");
        String loc = StrUtil.blankToDefault(location, DEFAULT_LOCATION);
        AiVideoUtils.checkArgument(ID_PATTERN.matcher(loc).matches(), "地域格式不正确");
        if (accessTokenSupplier == null) {
            throw new IllegalArgumentException("Vertex AI 的访问令牌提供者不能为空");
        }
        this.projectId = projectId;
        this.location = loc;
        this.baseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, defaultBaseUrl(loc)), "/");
        this.accessTokenSupplier = accessTokenSupplier;
        this.httpClient = new AiVideoHttpClient(PLATFORM, timeout,
                maxResponseSize > 0 ? maxResponseSize : DEFAULT_MAX_RESPONSE_SIZE);
    }

    public VertexVeoVideoApi(String projectId, String location, Supplier<String> accessTokenSupplier) {
        this(projectId, location, accessTokenSupplier, null, null, DEFAULT_MAX_RESPONSE_SIZE);
    }

    public static String defaultBaseUrl(String location) {
        return "global".equals(location) ? "https://aiplatform.googleapis.com"
                : "https://" + location + "-aiplatform.googleapis.com";
    }

    /**
     * 校验并构建请求体：不发请求
     */
    public static ObjectNode buildRequestBody(VertexVeoVideoRequest request) {
        // 1.1 模型、素材
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        AiVideoUtils.checkArgument(MODEL_PATTERN.matcher(model).matches(), "Veo 模型格式不正确，例如 {}", MODEL_3_1);
        boolean v31 = model.startsWith("veo-3.1-");
        boolean hasImage = request.getImage() != null;
        boolean hasLast = request.getLastFrame() != null;
        int referenceCount = CollUtil.size(request.getReferenceImages());
        AiVideoUtils.checkArgument(!hasLast || hasImage, "尾帧必须与首帧一起使用");
        AiVideoUtils.checkArgument(referenceCount == 0 || !hasImage, "参考图不能与首尾帧混用");
        AiVideoUtils.checkArgument(referenceCount <= MAX_REFERENCE_IMAGES, "参考图最多 {} 张", MAX_REFERENCE_IMAGES);
        if (referenceCount > 0) {
            AiVideoUtils.checkArgument(!MODEL_3_1_LITE.equals(model), "{} 不支持参考图", MODEL_3_1_LITE);
        }
        if (!hasImage) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        // 1.2 输出参数
        AiVideoUtils.checkIn(request.getAspectRatio(), ASPECT_RATIOS, "比例");
        AiVideoUtils.checkIn(request.getResolution(), RESOLUTIONS, "分辨率");
        if ("4k".equals(request.getResolution())) {
            AiVideoUtils.checkArgument(MODEL_3_1.equals(model), "只有 {} 支持 4k", MODEL_3_1);
        }
        if (v31) {
            AiVideoUtils.checkIn(request.getDurationSeconds(), DURATIONS_3_1, "Veo 3.1 时长");
            if (referenceCount > 0 && request.getDurationSeconds() != null) {
                AiVideoUtils.checkArgument(request.getDurationSeconds() == 8, "参考图生成视频只支持 8 秒");
            }
        } else {
            AiVideoUtils.checkRange(request.getDurationSeconds(), 4, 8, "时长");
        }
        AiVideoUtils.checkRange(request.getSampleCount(), 1, 4, "生成数量");
        AiVideoUtils.checkRange(request.getSeed(), 0, MAX_SEED, "随机种子");
        AiVideoUtils.checkIn(request.getPersonGeneration(), PERSON_GENERATIONS, "人物生成策略");
        AiVideoUtils.checkArgument(request.getStorageUri() == null || request.getStorageUri().startsWith("gs://"),
                "storageUri 必须为 gs:// 地址");

        // 2. 构建
        ObjectNode body = AiVideoUtils.newObject();
        ObjectNode instance = body.putArray("instances").addObject();
        if (request.getPrompt() != null) {
            instance.put("prompt", request.getPrompt());
        }
        if (hasImage) {
            instance.set("image", toImageNode(request.getImage(), "首帧图片"));
        }
        if (hasLast) {
            instance.set("lastFrame", toImageNode(request.getLastFrame(), "尾帧图片"));
        }
        if (referenceCount > 0) {
            ArrayNode references = instance.putArray("referenceImages");
            for (VertexVeoVideoRequest.Image image : request.getReferenceImages()) {
                ObjectNode reference = references.addObject();
                reference.set("image", toImageNode(image, "参考图"));
                reference.put("referenceType", "asset");
            }
        }
        ObjectNode parameters = body.putObject("parameters");
        putIfNotNull(parameters, "aspectRatio", request.getAspectRatio());
        if (request.getDurationSeconds() != null) {
            parameters.put("durationSeconds", request.getDurationSeconds());
        }
        putIfNotNull(parameters, "resolution", request.getResolution());
        if (request.getGenerateAudio() != null) {
            parameters.put("generateAudio", request.getGenerateAudio());
        }
        putIfNotNull(parameters, "negativePrompt", request.getNegativePrompt());
        putIfNotNull(parameters, "personGeneration", request.getPersonGeneration());
        if (request.getSampleCount() != null) {
            parameters.put("sampleCount", request.getSampleCount());
        }
        if (request.getSeed() != null) {
            parameters.put("seed", request.getSeed());
        }
        putIfNotNull(parameters, "storageUri", request.getStorageUri());
        if (request.getEnhancePrompt() != null) {
            parameters.put("enhancePrompt", request.getEnhancePrompt());
        }
        return body;
    }

    private static ObjectNode toImageNode(VertexVeoVideoRequest.Image image, String name) {
        boolean bytes = StrUtil.isNotBlank(image.getBytesBase64Encoded());
        boolean gcs = StrUtil.isNotBlank(image.getGcsUri());
        AiVideoUtils.checkArgument(bytes ^ gcs, "{}需要 bytesBase64Encoded 或 gcsUri（二选一）", name);
        AiVideoUtils.checkArgument(!gcs || image.getGcsUri().startsWith("gs://"), "{}的 gcsUri 必须为 gs:// 地址", name);
        AiVideoUtils.checkIn(image.getMimeType(), IMAGE_MIME_TYPES, name + "类型");
        AiVideoUtils.requireText(image.getMimeType(), name + "类型");
        ObjectNode node = AiVideoUtils.newObject();
        if (bytes) {
            node.put("bytesBase64Encoded", image.getBytesBase64Encoded());
        } else {
            node.put("gcsUri", image.getGcsUri());
        }
        node.put("mimeType", image.getMimeType());
        return node;
    }

    private static void putIfNotNull(ObjectNode node, String field, String value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private String modelPath(String model) {
        return "/v1/projects/" + projectId + "/locations/" + location + "/publishers/google/models/" + model;
    }

    /**
     * 创建视频生成任务（长时操作）：只请求一次，不重试
     *
     * @return 任务引用，taskId 为完整的 operation name，查询时需要
     */
    public AiVideoTaskRef createTask(VertexVeoVideoRequest request) {
        ObjectNode body = buildRequestBody(request);
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.POST,
                URI.create(baseUrl + modelPath(request.getModel()) + ":predictLongRunning"), headers(),
                AiVideoUtils.toJsonBytes(body), "提交 Veo 视频任务", true);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        if (!result.is2xx()) {
            throw buildException(result, root, true);
        }
        String name = AiVideoUtils.text(root, "name");
        if (name == null) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null, null, true,
                    "Veo 视频任务提交后未返回 operation name，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[createTask][Veo 视频任务已提交，model({}) operation({})]", request.getModel(), name);
        return new AiVideoTaskRef(PLATFORM, request.getModel(), name, null, null);
    }

    /**
     * 查询视频生成任务：调用模型的 fetchPredictOperation
     */
    public AiVideoTaskResult getTask(AiVideoTaskRef ref) {
        AiVideoUtils.checkArgument(ref != null, "任务引用不能为空");
        String model = AiVideoUtils.requireText(ref.getModel(), "模型");
        AiVideoUtils.checkArgument(MODEL_PATTERN.matcher(model).matches(), "Veo 模型格式不正确");
        String name = AiVideoUtils.requireText(ref.getTaskId(), "operation name");
        AiVideoUtils.checkArgument(name.matches("projects/[^/]{1,64}/locations/" + Pattern.quote(location)
                        + "/publishers/google/models/" + Pattern.quote(model) + "/operations/[A-Za-z0-9_\\-]{1,128}"),
                "operation name 与当前地域、模型不匹配");
        ObjectNode body = AiVideoUtils.newObject();
        body.put("operationName", name);
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.POST,
                URI.create(baseUrl + modelPath(model) + ":fetchPredictOperation"), headers(),
                AiVideoUtils.toJsonBytes(body), "查询 Veo 视频任务", false);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        if (!result.is2xx()) {
            throw buildException(result, root, false);
        }
        if (root == null || !root.isObject()) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null, null, false,
                    "查询 Veo 视频任务失败：返回格式不正确");
        }
        return parseOperation(name, root);
    }

    static AiVideoTaskResult parseOperation(String name, JsonNode root) {
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(name);
        boolean done = root.path("done").asBoolean(false);
        task.setRawStatus(done ? "done" : "running");
        if (!done) {
            task.setStatus(AiVideoTaskStatus.RUNNING);
            return task;
        }
        JsonNode error = root.get("error");
        if (error != null && error.isObject()) {
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorCode(StrUtil.blankToDefault(AiVideoUtils.text(error, "status"), AiVideoUtils.text(error, "code")));
            task.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(error, "message")));
            return task;
        }
        JsonNode response = root.get("response");
        Integer filtered = AiVideoUtils.integer(response, "raiMediaFilteredCount");
        task.putMetadata("raiMediaFilteredCount", filtered);
        List<String> reasons = new ArrayList<>();
        JsonNode reasonNodes = response != null ? response.get("raiMediaFilteredReasons") : null;
        if (reasonNodes != null && reasonNodes.isArray()) {
            for (JsonNode reason : reasonNodes) {
                reasons.add(reason.asText());
            }
        }
        JsonNode videos = response != null ? response.get("videos") : null;
        if (videos != null && videos.isArray()) {
            for (JsonNode item : videos) {
                AiVideoOutput output = new AiVideoOutput();
                output.setGcsUri(AiVideoUtils.text(item, "gcsUri"));
                String base64 = AiVideoUtils.text(item, "bytesBase64Encoded");
                if (base64 != null) {
                    try {
                        output.setData(Base64.getDecoder().decode(base64));
                    } catch (IllegalArgumentException ex) {
                        throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, null, null, null, false,
                                "查询 Veo 视频任务失败：视频内容不是合法的 base64");
                    }
                }
                output.setMimeType(StrUtil.blankToDefault(AiVideoUtils.text(item, "mimeType"), "video/mp4"));
                if (output.hasContent()) {
                    task.getVideos().add(output);
                }
            }
        }
        if (task.getVideos().isEmpty() && filtered != null && filtered > 0) {
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorCode(AiVideoTaskResult.ERROR_CODE_FILTERED);
            task.setErrorMessage(AiVideoUtils.sanitize("全部结果被安全策略过滤：" + StrUtil.join("; ", reasons)));
            return task;
        }
        return task.succeedOrFailIfEmpty();
    }

    private AiVideoException buildException(AiVideoHttpClient.HttpResult result, JsonNode root, boolean submit) {
        JsonNode error = root != null ? root.get("error") : null;
        String status = AiVideoUtils.text(error, "status");
        String message = StrUtil.blankToDefault(AiVideoUtils.text(error, "message"), "返回格式不正确");
        String reason = null;
        JsonNode details = error != null ? error.get("details") : null;
        if (details != null && details.isArray()) {
            for (JsonNode detail : details) {
                reason = StrUtil.blankToDefault(reason, AiVideoUtils.text(detail, "reason"));
            }
        }
        boolean unknown = submit && (result.getStatus() >= 500 || error == null);
        log.warn("[VertexVeo][{}失败，HTTP {}，status {}，reason {}]", submit ? "提交" : "查询", result.getStatus(), status, reason);
        return new AiVideoException(PLATFORM, getKind(status, reason, result.getStatus()), result.getStatus(),
                StrUtil.blankToDefault(reason, status), null, unknown,
                StrUtil.format("Veo 视频任务{}失败（HTTP {}，{}）：{}{}", submit ? "提交" : "查询", result.getStatus(),
                        StrUtil.blankToDefault(StrUtil.blankToDefault(reason, status), "-"), AiVideoUtils.sanitize(message),
                        unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
    }

    static AiVideoException.Kind getKind(String status, String reason, int httpStatus) {
        if ("BILLING_DISABLED".equals(reason)) {
            return AiVideoException.Kind.INSUFFICIENT_BALANCE;
        }
        if ("SERVICE_DISABLED".equals(reason)) {
            return AiVideoException.Kind.PERMISSION;
        }
        if ("UNAUTHENTICATED".equals(status)) {
            return AiVideoException.Kind.AUTHENTICATION;
        }
        if ("PERMISSION_DENIED".equals(status)) {
            return AiVideoException.Kind.PERMISSION;
        }
        if ("RESOURCE_EXHAUSTED".equals(status)) {
            return AiVideoException.Kind.RATE_LIMITED;
        }
        if ("NOT_FOUND".equals(status)) {
            return AiVideoException.Kind.NOT_FOUND;
        }
        if ("INVALID_ARGUMENT".equals(status) || "FAILED_PRECONDITION".equals(status) || "OUT_OF_RANGE".equals(status)) {
            return AiVideoException.Kind.INVALID_REQUEST;
        }
        if ("INTERNAL".equals(status) || "UNAVAILABLE".equals(status) || "DEADLINE_EXCEEDED".equals(status)) {
            return AiVideoException.Kind.SERVER_ERROR;
        }
        return AiVideoException.kindOfHttpStatus(httpStatus);
    }

    private Map<String, String> headers() {
        String token = accessTokenSupplier.get();
        if (StrUtil.isBlank(token)) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.AUTHENTICATION, null, null, null, false,
                    "Vertex AI 访问令牌为空");
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + token.trim());
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }

}
