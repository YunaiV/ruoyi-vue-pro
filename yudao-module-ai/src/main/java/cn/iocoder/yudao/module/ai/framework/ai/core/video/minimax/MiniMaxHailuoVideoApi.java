package cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax;

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
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MiniMax 海螺旧模型视频生成 API（v1 接口）：兼容 Hailuo 2.3 / 2.3 Fast / 02 / 01 系列
 *
 * 1. 创建：POST {baseUrl}/v1/video_generation，Bearer API Key；只发一次。注意 v1 的业务错误也是 HTTP 200，
 *    以 base_resp.status_code 判断（0 为成功）
 * 2. 查询：GET {baseUrl}/v1/query/video_generation?task_id=；状态 Preparing、Queueing、Processing、Success、Fail
 * 3. 成功只返回 file_id，需要再 GET {baseUrl}/v1/files/retrieve?file_id= 换取 file.download_url
 * 4. 各模型支持的生成方式、分辨率与时长组合不同（见 {@link #MODEL_SPECS}），例如 MiniMax-Hailuo-2.3-Fast 只支持图生
 * 5. 新模型 MiniMax-H3 走 v2 接口，见 {@link MiniMaxH3VideoApi}
 *
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-t2v">文生视频</a>
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-i2v">图生视频</a>
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-fl2v">首尾帧生视频</a>
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-s2v">主体参考生视频</a>
 * @see <a href="https://platform.minimax.io/docs/api-reference/video-generation-query">查询视频生成任务</a>
 * @see <a href="https://platform.minimax.io/docs/api-reference/file-management-retrieve">文件检索</a>
 * @author 芋道源码
 */
@Slf4j
public class MiniMaxHailuoVideoApi {

    public static final String PLATFORM = "minimax-v1";
    public static final String DEFAULT_BASE_URL = MiniMaxH3VideoApi.DEFAULT_BASE_URL;

    /**
     * 生成方式
     */
    public enum Mode {
        /**
         * 文生
         */
        T2V,
        /**
         * 图生（首帧）
         */
        I2V,
        /**
         * 首尾帧
         */
        FL2V,
        /**
         * 主体参考
         */
        S2V
    }

    /**
     * 模型 → 生成方式 → 时长 → 可选分辨率（第一个为默认）
     */
    public static final Map<String, Map<Mode, Map<Integer, List<String>>>> MODEL_SPECS = new LinkedHashMap<>();

    static {
        Map<Integer, List<String>> hailuo = durations(ListUtil.of("768P", "1080P"), ListUtil.of("768P"));
        MODEL_SPECS.put("MiniMax-Hailuo-2.3", modes(Mode.T2V, hailuo, Mode.I2V, hailuo));
        MODEL_SPECS.put("MiniMax-Hailuo-2.3-Fast", modes(Mode.I2V, hailuo));
        Map<Mode, Map<Integer, List<String>>> hailuo02 = modes(Mode.T2V, hailuo,
                Mode.I2V, durations(ListUtil.of("768P", "512P", "1080P"), ListUtil.of("768P", "512P")));
        hailuo02.put(Mode.FL2V, hailuo);
        MODEL_SPECS.put("MiniMax-Hailuo-02", hailuo02);
        Map<Integer, List<String>> legacy = durations(ListUtil.of("720P"), null);
        MODEL_SPECS.put("T2V-01-Director", modes(Mode.T2V, legacy));
        MODEL_SPECS.put("T2V-01", modes(Mode.T2V, legacy));
        MODEL_SPECS.put("I2V-01-Director", modes(Mode.I2V, legacy));
        MODEL_SPECS.put("I2V-01-live", modes(Mode.I2V, legacy));
        MODEL_SPECS.put("I2V-01", modes(Mode.I2V, legacy));
        // S2V-01：文档未给出时长、分辨率，不允许传
        MODEL_SPECS.put("S2V-01", modes(Mode.S2V, Collections.<Integer, List<String>>emptyMap()));
    }

    /**
     * 支持 fast_pretreatment 的模型
     */
    private static final List<String> FAST_PRETREATMENT_MODELS = ListUtil.of("MiniMax-Hailuo-2.3",
            "MiniMax-Hailuo-2.3-Fast", "MiniMax-Hailuo-02");
    private static final int DEFAULT_DURATION = 6;
    private static final int MAX_PROMPT_LENGTH = 2000;
    private static final int STATUS_CODE_SUCCESS = 0;

    private final String baseUrl;
    private final String apiKey;
    private final AiVideoHttpClient httpClient;

    public MiniMaxHailuoVideoApi(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, null);
    }

    public MiniMaxHailuoVideoApi(String baseUrl, String apiKey, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("MiniMax 的 API Key 不能为空");
        }
        this.baseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/");
        this.apiKey = apiKey;
        this.httpClient = new AiVideoHttpClient(PLATFORM, timeout, AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE, apiKey);
    }

    private static Map<Integer, List<String>> durations(List<String> sixSeconds, List<String> tenSeconds) {
        Map<Integer, List<String>> map = new LinkedHashMap<>();
        map.put(6, sixSeconds);
        if (tenSeconds != null) {
            map.put(10, tenSeconds);
        }
        return map;
    }

    private static Map<Mode, Map<Integer, List<String>>> modes(Mode mode, Map<Integer, List<String>> spec) {
        Map<Mode, Map<Integer, List<String>>> map = new EnumMap<>(Mode.class);
        map.put(mode, spec);
        return map;
    }

    private static Map<Mode, Map<Integer, List<String>>> modes(Mode mode1, Map<Integer, List<String>> spec1,
                                                               Mode mode2, Map<Integer, List<String>> spec2) {
        Map<Mode, Map<Integer, List<String>>> map = modes(mode1, spec1);
        map.put(mode2, spec2);
        return map;
    }

    /**
     * 根据素材判断生成方式
     */
    public static Mode getMode(MiniMaxHailuoVideoRequest request) {
        if (StrUtil.isNotBlank(request.getSubjectReferenceImage())) {
            return Mode.S2V;
        }
        if (StrUtil.isNotBlank(request.getLastFrameImage())) {
            return Mode.FL2V;
        }
        if (StrUtil.isNotBlank(request.getFirstFrameImage())) {
            return Mode.I2V;
        }
        return Mode.T2V;
    }

    /**
     * 校验并构建请求体：不发请求
     */
    public static ObjectNode buildRequestBody(MiniMaxHailuoVideoRequest request) {
        // 1.1 模型、生成方式
        String model = AiVideoUtils.requireText(request.getModel(), "模型");
        Map<Mode, Map<Integer, List<String>>> spec = MODEL_SPECS.get(model);
        AiVideoUtils.checkArgument(spec != null, "MiniMax v1 不支持模型({})，可选：{}", model, MODEL_SPECS.keySet());
        Mode mode = getMode(request);
        AiVideoUtils.checkArgument(spec.containsKey(mode), "模型({})不支持{}，只支持：{}", model, getModeName(mode),
                getModeNames(spec.keySet()));
        AiVideoUtils.checkArgument(mode != Mode.S2V || (StrUtil.isBlank(request.getFirstFrameImage())
                && StrUtil.isBlank(request.getLastFrameImage())), "主体参考不能与首尾帧同时使用");
        // 1.2 提示词
        if (mode == Mode.T2V) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        // 1.3 时长、分辨率组合
        Map<Integer, List<String>> durations = spec.get(mode);
        if (durations.isEmpty()) {
            AiVideoUtils.checkArgument(request.getDuration() == null && request.getResolution() == null,
                    "模型({})不支持指定时长、分辨率", model);
        } else {
            int duration = request.getDuration() != null ? request.getDuration() : DEFAULT_DURATION;
            List<String> resolutions = durations.get(duration);
            AiVideoUtils.checkArgument(resolutions != null, "模型({}){}不支持时长 {} 秒，可选：{}", model, getModeName(mode),
                    duration, durations.keySet());
            if (request.getResolution() != null) {
                AiVideoUtils.checkArgument(resolutions.contains(request.getResolution()),
                        "模型({}){} {} 秒不支持分辨率 {}，可选：{}", model, getModeName(mode), duration,
                        request.getResolution(), resolutions);
            }
        }
        if (request.getFastPretreatment() != null) {
            AiVideoUtils.checkArgument(FAST_PRETREATMENT_MODELS.contains(model), "模型({})不支持 fast_pretreatment", model);
        }

        // 2. 构建
        ObjectNode body = AiVideoUtils.newObject();
        body.put("model", model);
        if (request.getPrompt() != null) {
            body.put("prompt", request.getPrompt());
        }
        if (request.getPromptOptimizer() != null) {
            body.put("prompt_optimizer", request.getPromptOptimizer());
        }
        if (request.getFastPretreatment() != null) {
            body.put("fast_pretreatment", request.getFastPretreatment());
        }
        if (request.getDuration() != null) {
            body.put("duration", request.getDuration());
        }
        if (request.getResolution() != null) {
            body.put("resolution", request.getResolution());
        }
        if (StrUtil.isNotBlank(request.getFirstFrameImage())) {
            body.put("first_frame_image", AiVideoUtils.checkMediaUrl(request.getFirstFrameImage(), "首帧图片"));
        }
        if (StrUtil.isNotBlank(request.getLastFrameImage())) {
            body.put("last_frame_image", AiVideoUtils.checkMediaUrl(request.getLastFrameImage(), "尾帧图片"));
        }
        if (mode == Mode.S2V) {
            ObjectNode subject = body.putArray("subject_reference").addObject();
            subject.put("type", "character");
            subject.putArray("image").add(AiVideoUtils.checkMediaUrl(request.getSubjectReferenceImage(), "主体参考图片"));
        }
        if (request.getCallbackUrl() != null) {
            body.put("callback_url", AiVideoUtils.checkMediaUrl(request.getCallbackUrl(), "回调地址"));
        }
        return body;
    }

    private static String getModeName(Mode mode) {
        switch (mode) {
            case T2V:
                return "文生视频";
            case I2V:
                return "图生视频";
            case FL2V:
                return "首尾帧生视频";
            default:
                return "主体参考生视频";
        }
    }

    private static List<String> getModeNames(Iterable<Mode> modes) {
        List<String> names = new java.util.ArrayList<>();
        for (Mode mode : modes) {
            names.add(getModeName(mode));
        }
        return names;
    }

    /**
     * 创建视频生成任务：只请求一次，不重试
     */
    public AiVideoTaskRef createTask(MiniMaxHailuoVideoRequest request) {
        ObjectNode body = buildRequestBody(request);
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.POST, URI.create(baseUrl + "/v1/video_generation"),
                headers(), AiVideoUtils.toJsonBytes(body), "提交 MiniMax 海螺视频任务", true);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        checkBaseResp(result, root, true, "提交 MiniMax 海螺视频任务");
        String taskId = AiVideoUtils.text(root, "task_id");
        if (taskId == null) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null, null, true,
                    "MiniMax 海螺视频任务提交后未返回 task_id，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[createTask][MiniMax 海螺视频任务已提交，model({}) taskId({})]", request.getModel(), taskId);
        return new AiVideoTaskRef(PLATFORM, request.getModel(), taskId, null, null);
    }

    /**
     * 查询视频生成任务：成功时再检索文件，换取下载地址
     */
    public AiVideoTaskResult getTask(String taskId) {
        AiVideoUtils.requireText(taskId, "任务编号");
        AiVideoUtils.checkArgument(taskId.matches("[A-Za-z0-9_-]{1,128}"), "任务编号格式不正确");
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.GET,
                URI.create(baseUrl + "/v1/query/video_generation?task_id=" + taskId), headers(), null,
                "查询 MiniMax 海螺视频任务", false);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        String status = AiVideoUtils.text(root, "status");
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(taskId);
        task.setRawStatus(status);
        // 失败的任务：base_resp 可能带审核错误码（1026 输入敏感、1027 输出敏感），按任务失败返回
        if ("Fail".equals(status)) {
            JsonNode baseResp = root.path("base_resp");
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorCode(AiVideoUtils.text(baseResp, "status_code"));
            task.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(baseResp, "status_msg"), apiKey));
            return task;
        }
        checkBaseResp(result, root, false, "查询 MiniMax 海螺视频任务");
        if ("Preparing".equals(status) || "Queueing".equals(status)) {
            task.setStatus(AiVideoTaskStatus.QUEUED);
        } else if ("Processing".equals(status)) {
            task.setStatus(AiVideoTaskStatus.RUNNING);
        } else if ("Success".equals(status)) {
            String fileId = AiVideoUtils.text(root, "file_id");
            if (fileId != null) {
                AiVideoOutput output = retrieveFile(fileId);
                output.setWidth(AiVideoUtils.integer(root, "video_width"));
                output.setHeight(AiVideoUtils.integer(root, "video_height"));
                task.getVideos().add(output);
                task.putMetadata("fileId", fileId);
            }
            task.succeedOrFailIfEmpty();
        } else {
            task.setStatus(AiVideoTaskStatus.UNKNOWN);
        }
        return task;
    }

    /**
     * 检索文件，获得下载地址
     */
    public AiVideoOutput retrieveFile(String fileId) {
        AiVideoUtils.checkArgument(fileId != null && fileId.matches("\\d{1,32}"), "文件编号格式不正确");
        AiVideoHttpClient.HttpResult result = httpClient.execute(HttpMethod.GET,
                URI.create(baseUrl + "/v1/files/retrieve?file_id=" + fileId), headers(), null, "检索 MiniMax 视频文件", false);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        checkBaseResp(result, root, false, "检索 MiniMax 视频文件");
        JsonNode file = root.get("file");
        AiVideoOutput output = new AiVideoOutput();
        output.setUrl(AiVideoUtils.text(file, "download_url"));
        output.setMimeType("video/mp4");
        return output;
    }

    private void checkBaseResp(AiVideoHttpClient.HttpResult result, JsonNode root, boolean submit, String action) {
        JsonNode baseResp = root != null ? root.get("base_resp") : null;
        Integer code = AiVideoUtils.integer(baseResp, "status_code");
        if (result.is2xx() && code != null && code == STATUS_CODE_SUCCESS) {
            return;
        }
        String message = baseResp != null ? AiVideoUtils.text(baseResp, "status_msg") : null;
        String codeText = code != null ? String.valueOf(code) : null;
        AiVideoException.Kind kind = code != null
                ? MiniMaxH3VideoApi.getKind(null, codeText, result.getStatus(), StrUtil.nullToEmpty(message))
                : AiVideoException.kindOfHttpStatus(result.getStatus());
        if (code != null && kind == AiVideoException.Kind.INVALID_RESPONSE) {
            kind = AiVideoException.Kind.INVALID_REQUEST;
        }
        // 2xx 却解析不到 base_resp、5xx：创建任务是否成功未知
        boolean unknown = submit && (result.getStatus() >= 500 || code == null);
        log.warn("[MiniMaxHailuo][{}失败，HTTP {}，status_code {}]", action, result.getStatus(), codeText);
        throw new AiVideoException(PLATFORM, kind, result.getStatus(), codeText, null, unknown,
                StrUtil.format("{}失败（HTTP {}，status_code={}）：{}{}", action, result.getStatus(),
                        StrUtil.blankToDefault(codeText, "-"),
                        StrUtil.isBlank(message) ? "返回格式不正确" : AiVideoUtils.sanitize(message, apiKey),
                        unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
