package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

/**
 * RunPod Serverless ComfyUI 代理服务。
 *
 * <p>职责：
 * <ol>
 *   <li>代理前端的出图请求，在服务器端持有 RUNPOD_API_KEY，浏览器不可见。</li>
 *   <li>{@link #submitJob} — 向 RunPod 提交 ComfyUI 工作流（/run），立即返回 jobId。</li>
 *   <li>{@link #queryStatus} — 轮询 RunPod /status/{jobId}，返回状态 + 输出图片。</li>
 *   <li>{@link #parseImages} — 解析 output 字段，统一返回 base64 data-URL 列表。</li>
 * </ol>
 * </p>
 *
 * <p>配置项（application.yml / 环境变量）：
 * <pre>
 * deepay:
 *   runpod:
 *     api-key:     ${RUNPOD_API_KEY:}           # RunPod API Key，务必通过环境变量注入，不要写死
 *     endpoint-id: ${RUNPOD_ENDPOINT_ID:}       # Serverless ComfyUI Endpoint ID
 * </pre>
 * </p>
 *
 * <p>RunPod Serverless ComfyUI 工作流格式：
 * <pre>
 * POST https://api.runpod.ai/v2/{endpointId}/run
 * {
 *   "input": {
 *     "workflow": { ... ComfyUI workflow_api.json ... }
 *   }
 * }
 * </pre>
 * </p>
 *
 * <p>output 字段格式（runpod-worker-comfy 官方 Worker）：
 * <pre>
 * { "message": ["data:image/png;base64,xxx", ...] }  // base64 内嵌
 * 或
 * { "images": ["https://cdn.xxx/xxx.png", ...] }      // CDN URL（若 Worker 已上传 S3）
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class RunPodComfyService {

    private static final String RUNPOD_BASE = "https://api.runpod.ai/v2";

    @Value("${deepay.runpod.api-key:}")
    private String apiKey;

    @Value("${deepay.runpod.endpoint-id:}")
    private String endpointId;

    private final RestTemplate restTemplate;

    public RunPodComfyService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 是否已完整配置（API Key + Endpoint ID 均不为空）。
     * 调用方可用此方法判断是否启用 RunPod 路径。
     */
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(endpointId);
    }

    /**
     * 提交 ComfyUI 工作流到 RunPod Serverless（异步，立即返回 jobId）。
     *
     * @param workflowJson ComfyUI workflow_api.json 对象（已反序列化的 Map）
     * @return jobId（RunPod 任务 ID，用于后续 queryStatus 轮询）
     * @throws IllegalStateException 若未配置 API Key / Endpoint
     * @throws RuntimeException      若 RunPod API 调用失败
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String submitJob(Map<String, Object> workflowJson) {
        assertConfigured();

        HttpHeaders headers = makeHeaders();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", Collections.singletonMap("workflow", workflowJson));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = RUNPOD_BASE + "/" + endpointId + "/run";

        log.info("[RunPodComfy] 提交工作流 endpoint={}", endpointId);
        Map response;
        try {
            response = restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            log.error("[RunPodComfy] 提交失败 endpoint={}", endpointId, e);
            throw new RuntimeException("RunPod 提交失败: " + e.getMessage(), e);
        }

        if (response == null || !response.containsKey("id")) {
            throw new RuntimeException("RunPod 响应异常，缺少 id 字段: " + response);
        }

        String jobId = String.valueOf(response.get("id"));
        log.info("[RunPodComfy] 已提交，jobId={}", jobId);
        return jobId;
    }

    /**
     * 查询 RunPod 任务状态。
     *
     * @param jobId submitJob 返回的任务 ID
     * @return 状态对象，包含：
     *   <ul>
     *     <li>{@code status}  — IN_QUEUE / IN_PROGRESS / COMPLETED / FAILED / CANCELLED</li>
     *     <li>{@code images}  — 图片列表（仅 COMPLETED 时有值，已归一化为 data-URL 或 https URL）</li>
     *     <li>{@code error}   — 错误信息（仅 FAILED 时有值）</li>
     *     <li>{@code rawOutput} — RunPod 原始 output 字段</li>
     *   </ul>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JobStatusResult queryStatus(String jobId) {
        assertConfigured();

        String url = RUNPOD_BASE + "/" + endpointId + "/status/" + jobId;
        HttpEntity<Void> request = new HttpEntity<>(makeHeaders());

        Map response;
        try {
            response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class).getBody();
        } catch (Exception e) {
            log.warn("[RunPodComfy] 查询状态失败 jobId={}", jobId, e);
            return JobStatusResult.error(jobId, "网络错误: " + e.getMessage());
        }

        if (response == null) {
            return JobStatusResult.error(jobId, "RunPod 返回空响应");
        }

        String status = String.valueOf(response.getOrDefault("status", "UNKNOWN"));
        Object output = response.get("output");
        String error  = response.containsKey("error") ? String.valueOf(response.get("error")) : null;

        log.debug("[RunPodComfy] 状态查询 jobId={} status={}", jobId, status);

        if ("COMPLETED".equals(status)) {
            List<String> images = parseImages(output);
            return JobStatusResult.completed(jobId, images, output);
        }

        if ("FAILED".equals(status) || "CANCELLED".equals(status) || "TIMED_OUT".equals(status)) {
            return JobStatusResult.error(jobId, error != null ? error : status);
        }

        return JobStatusResult.pending(jobId, status);
    }

    /**
     * 解析 RunPod output 字段，统一返回图片 URL / data-URL 列表。
     *
     * <p>支持的 runpod-worker-comfy 输出格式：
     * <ul>
     *   <li>{@code { "message": ["data:image/png;base64,xxx", ...] }} — 官方 Worker 默认格式</li>
     *   <li>{@code { "images": ["https://cdn.xxx/a.png", ...] }}</li>
     *   <li>{@code [ "data:image/png;base64,xxx", ... ]} — 直接数组</li>
     *   <li>{@code "data:image/png;base64,xxx"} — 单个字符串</li>
     * </ul>
     * </p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<String> parseImages(Object output) {
        if (output == null) return Collections.emptyList();

        List<Object> rawList = null;

        if (output instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) output;

            // 官方 runpod-worker-comfy 格式: { "message": [...] }
            if (map.containsKey("message")) {
                Object msg = map.get("message");
                if (msg instanceof List)   rawList = (List<Object>) msg;
                else if (msg instanceof String) rawList = Collections.singletonList(msg);
            }
            // { "images": [...] }
            else if (map.containsKey("images")) {
                Object imgs = map.get("images");
                if (imgs instanceof List) rawList = (List<Object>) imgs;
            }
            // { "output": { ... } }  — nested
            else if (map.containsKey("output") && map.get("output") instanceof Map) {
                return parseImages(map.get("output"));
            }
        } else if (output instanceof List) {
            rawList = (List<Object>) output;
        } else if (output instanceof String) {
            rawList = Collections.singletonList(output);
        }

        if (rawList == null || rawList.isEmpty()) {
            log.warn("[RunPodComfy] 无法从 output 中解析图片列表，output 类型={}", output.getClass().getSimpleName());
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        for (Object item : rawList) {
            if (!(item instanceof String)) continue;
            String s = (String) item;
            if (s.startsWith("http") || s.startsWith("data:")) {
                result.add(s);
            } else if (!s.isEmpty()) {
                // 裸 base64 → 补充 data-URL 前缀
                result.add("data:image/png;base64," + s);
            }
        }
        return result;
    }

    // ── 内部工具 ────────────────────────────────────────────────────

    private void assertConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                "RunPod 未配置，请在 application.yml 中设置 deepay.runpod.api-key 和 deepay.runpod.endpoint-id"
            );
        }
    }

    private HttpHeaders makeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    // ── 结果 VO ──────────────────────────────────────────────────────

    public static class JobStatusResult {
        public final String       jobId;
        public final String       status;    // IN_QUEUE | IN_PROGRESS | COMPLETED | FAILED
        public final List<String> images;    // 仅 COMPLETED 时有值
        public final String       error;     // 仅 FAILED 时有值
        public final Object       rawOutput;

        private JobStatusResult(String jobId, String status, List<String> images, String error, Object rawOutput) {
            this.jobId     = jobId;
            this.status    = status;
            this.images    = images != null ? images : Collections.emptyList();
            this.error     = error;
            this.rawOutput = rawOutput;
        }

        public static JobStatusResult pending(String jobId, String status) {
            return new JobStatusResult(jobId, status, null, null, null);
        }

        public static JobStatusResult completed(String jobId, List<String> images, Object rawOutput) {
            return new JobStatusResult(jobId, "COMPLETED", images, null, rawOutput);
        }

        public static JobStatusResult error(String jobId, String error) {
            return new JobStatusResult(jobId, "FAILED", null, error, null);
        }

        public boolean isDone() {
            return "COMPLETED".equals(status) || "FAILED".equals(status);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("jobId",  jobId);
            m.put("status", status);
            if (!images.isEmpty()) m.put("images", images);
            if (error != null)     m.put("error",  error);
            return m;
        }
    }
}
