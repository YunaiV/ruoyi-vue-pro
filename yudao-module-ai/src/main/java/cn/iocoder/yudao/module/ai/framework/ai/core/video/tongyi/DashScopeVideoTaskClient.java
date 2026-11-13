package cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoHttpClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoOutput;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskStatus;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisOutput;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisUsage;
import com.alibaba.dashscope.base.AsyncTaskParam;
import com.alibaba.dashscope.common.Status;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.ApiServiceOption;
import com.alibaba.dashscope.protocol.AsyncTaskOption;
import com.alibaba.dashscope.protocol.HalfDuplexRequest;
import com.alibaba.dashscope.protocol.HttpMethod;
import com.alibaba.dashscope.protocol.Protocol;
import com.alibaba.dashscope.protocol.StreamingMode;
import com.alibaba.dashscope.protocol.okhttp.OkHttpHttpClient;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;

/**
 * 阿里云百炼（DashScope）视频生成的异步任务客户端：通义万相、HappyHorse 共用，基于官方 SDK（dashscope-sdk-java）
 *
 * 1. 请求体（{@link VideoSynthesisParam}）、请求头（Authorization、X-DashScope-Async 等）、地址拼接（{@link HalfDuplexRequest}）、
 *    发送与错误解析（{@link OkHttpHttpClient}、{@link ApiException}）、结果（{@link VideoSynthesisResult}）均由 SDK 完成
 * 2. 不使用 SDK 的 VideoSynthesis / AsynchronousApi 门面：二者固定使用 SDK 进程级共享的 OkHttpClient
 *    （未关闭「连接失败重试」，网络日志级别受环境变量 DASHSCOPE_NETWORK_LOGGING_LEVEL 控制），且 call / wait 会在内部无限轮询。
 *    这里为每个实例创建独立的 OkHttpClient，通过 SDK 公开的 {@link OkHttpHttpClient#OkHttpHttpClient(OkHttpClient)} 使用：
 *    a. 关闭连接失败重试与重定向，并去掉响应的 Retry-After 头（OkHttp 会对「503 + Retry-After: 0」自动重发），付费的创建请求只发一次；
 *    b. 单次请求超时、响应体上限，不加网络日志拦截器；
 *    c. 地址（地域、业务空间）只在本实例生效，不修改 SDK 的全局 Constants，不影响通义 OCR、TinyFlow 等其它调用
 * 3. 创建：POST {baseUrl}/api/v1/services/aigc/video-generation/video-synthesis（X-DashScope-Async: enable）
 * 4. 查询：GET {baseUrl}/api/v1/tasks/{task_id}；task_status 为 PENDING、RUNNING、SUCCEEDED、FAILED、CANCELED、UNKNOWN，
 *    其中 UNKNOWN 表示任务不存在、状态未知或超过 24 小时，不能视为成功
 * 5. 地址：推荐业务空间专属域名 https://{WorkspaceId}.{region}.maas.aliyuncs.com，旧域名 https://dashscope.aliyuncs.com（北京）
 *    仍可用；模型、地址、API Key 须属于同一地域
 * 6. 结果 output.video_url 有效期 24 小时，需尽快转存
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/happyhorse-text-to-video-api-reference">HappyHorse 文生视频</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/text-to-video-api-reference">万相文生视频</a>
 * @author 芋道源码
 */
@Slf4j
public class DashScopeVideoTaskClient {

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";
    public static final String API_PATH = "/api/v1";
    /**
     * 与 SDK VideoSynthesis 相同的服务路径：/services/aigc/video-generation/video-synthesis
     */
    private static final String TASK_GROUP = "aigc";
    private static final String TASK = "video-generation";
    private static final String FUNCTION = "video-synthesis";
    private static final String OSS_RESOURCE_RESOLVE_HEADER = "X-DashScope-OssResourceResolve";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final String platform;
    /**
     * SDK 的 baseHttpUrl：{baseUrl}/api/v1
     */
    private final String apiBaseUrl;
    private final String apiKey;
    private final OkHttpHttpClient client;

    public DashScopeVideoTaskClient(String platform, String baseUrl, String apiKey, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("阿里云百炼的 API Key 不能为空");
        }
        this.platform = platform;
        this.apiBaseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/") + API_PATH;
        this.apiKey = apiKey;
        this.client = new OkHttpHttpClient(buildOkHttpClient(timeout != null ? timeout : AiVideoHttpClient.DEFAULT_TIMEOUT,
                AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE));
    }

    /**
     * 业务空间专属域名，例如 workspaceBaseUrl("ws-xxx", "cn-beijing")
     */
    public static String workspaceBaseUrl(String workspaceId, String region) {
        AiVideoUtils.checkArgument(StrUtil.isNotBlank(workspaceId) && workspaceId.matches("[A-Za-z0-9-]{1,64}"),
                "业务空间 ID 格式不正确");
        AiVideoUtils.checkArgument(StrUtil.isNotBlank(region) && region.matches("[a-z0-9-]{1,32}"), "地域格式不正确");
        return "https://" + workspaceId + "." + region + ".maas.aliyuncs.com";
    }

    // ========== 请求参数 ==========

    /**
     * 创建 SDK 请求参数的构建器：去掉 SDK 对旧模型的默认值（input.extend_prompt=true、parameters.with_audio=false），
     * 避免发送新模型文档中没有的字段；其它字段只有设置了才发送
     */
    public static VideoSynthesisParam.VideoSynthesisParamBuilder<?, ?> newParam(String model) {
        return VideoSynthesisParam.builder().model(model).extendPrompt(null).withAudio(null);
    }

    /**
     * 素材 oss:// 临时地址需要服务端解析：任一地址为 oss:// 时，加请求头 X-DashScope-OssResourceResolve: enable
     */
    public static void resolveOssResources(VideoSynthesisParam.VideoSynthesisParamBuilder<?, ?> builder, String... urls) {
        for (String url : urls) {
            if (url != null && StrUtil.startWithIgnoreCase(url.trim(), "oss://")) {
                builder.header(OSS_RESOURCE_RESOLVE_HEADER, "enable");
                return;
            }
        }
    }

    /**
     * SDK 实际发送的请求体（model、input、parameters），用于测试与排查
     */
    public static ObjectNode toJson(VideoSynthesisParam param) {
        return (ObjectNode) AiVideoUtils.parseJson(JsonUtils.toJson(param.getHttpBody()));
    }

    // ========== 创建、查询 ==========

    /**
     * 提交异步任务：只请求一次，不重试
     *
     * @param param 已校验的请求参数（不含 API Key，由本客户端设置）
     * @param action 动作描述
     */
    public AiVideoTaskRef submit(VideoSynthesisParam param, String action) {
        param.setApiKey(apiKey);
        ApiServiceOption option = ApiServiceOption.builder().protocol(Protocol.HTTP).httpMethod(HttpMethod.POST)
                .streamingMode(StreamingMode.NONE).taskGroup(TASK_GROUP).task(TASK).function(FUNCTION)
                .isAsyncTask(true).baseHttpUrl(apiBaseUrl).build();
        VideoSynthesisResult result = send(new HalfDuplexRequest(param, option), action, true);
        String taskId = result.getOutput() != null ? result.getOutput().getTaskId() : null;
        if (StrUtil.isBlank(taskId)) {
            throw new AiVideoException(platform, AiVideoException.Kind.INVALID_RESPONSE, result.getStatusCode(),
                    StrUtil.emptyToNull(result.getCode()), result.getRequestId(), true,
                    action + "后未返回 task_id，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[submit][{} 已提交，model({}) taskId({})]", platform, param.getModel(), taskId);
        return new AiVideoTaskRef(platform, param.getModel(), taskId, null, result.getRequestId());
    }

    /**
     * 查询异步任务：只查询一次
     */
    @SuppressWarnings("deprecation") // AsyncTaskParam 是 SDK 自身 fetch 使用的参数类
    public AiVideoTaskResult query(String taskId, String action) {
        AiVideoUtils.requireText(taskId, "任务编号");
        AiVideoUtils.checkArgument(taskId.matches("[A-Za-z0-9_-]{1,128}"), "任务编号格式不正确");
        AsyncTaskOption option = AsyncTaskOption.builder().protocol(Protocol.HTTP).httpMethod(HttpMethod.GET)
                .baseHttpUrl(apiBaseUrl).url("/tasks/" + taskId).build();
        AsyncTaskParam param = AsyncTaskParam.builder().taskId(taskId).apiKey(apiKey).build();
        VideoSynthesisResult result = send(new HalfDuplexRequest(param, option), action, false);
        if (result.getOutput() == null) {
            throw new AiVideoException(platform, AiVideoException.Kind.INVALID_RESPONSE, result.getStatusCode(), null,
                    result.getRequestId(), false, action + "失败：返回格式不正确");
        }
        return parseTask(taskId, result);
    }

    static AiVideoTaskResult parseTask(String taskId, VideoSynthesisResult result) {
        VideoSynthesisOutput output = result.getOutput();
        VideoSynthesisUsage usage = result.getUsage();
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(StrUtil.blankToDefault(output.getTaskId(), taskId));
        task.setRequestId(result.getRequestId());
        String status = output.getTaskStatus();
        task.setRawStatus(status);
        task.putMetadata("submitTime", output.getSubmitTime());
        task.putMetadata("endTime", output.getEndTime());
        task.putMetadata("origPrompt", output.getOrigPrompt());
        if (usage != null) {
            task.putMetadata("usage.duration", toText(usage.getDuration()));
            task.putMetadata("usage.outputVideoDuration", toText(usage.getOutputVideoDuration()));
            task.putMetadata("usage.SR", usage.getSR());
            task.putMetadata("usage.size", usage.getSize());
            task.putMetadata("usage.ratio", usage.getRatio());
        }
        if ("PENDING".equals(status)) {
            task.setStatus(AiVideoTaskStatus.QUEUED);
        } else if ("RUNNING".equals(status)) {
            task.setStatus(AiVideoTaskStatus.RUNNING);
        } else if ("SUCCEEDED".equals(status)) {
            if (StrUtil.isNotBlank(output.getVideoUrl())) {
                AiVideoOutput video = new AiVideoOutput();
                video.setUrl(output.getVideoUrl());
                video.setMimeType("video/mp4");
                String duration = usage != null ? toText(usage.getOutputVideoDuration()) : null;
                video.setDurationSeconds(duration != null ? Double.valueOf(duration) : null);
                task.getVideos().add(video);
            }
            task.succeedOrFailIfEmpty();
        } else if ("FAILED".equals(status)) {
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorCode(output.getCode());
            task.setErrorMessage(AiVideoUtils.sanitize(output.getMessage()));
        } else if ("CANCELED".equals(status)) {
            task.setStatus(AiVideoTaskStatus.CANCELLED);
        } else {
            // UNKNOWN：任务不存在、状态未知或超过 24 小时；其它未识别的状态同样按未知处理
            task.setStatus(AiVideoTaskStatus.UNKNOWN);
        }
        return task;
    }

    /**
     * SDK 把数值解析为 Float：按十进制文本转换（3.0 → 3，2.04 → 2.04），避免二进制误差
     */
    private static String toText(Float value) {
        return value != null ? new BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString() : null;
    }

    private VideoSynthesisResult send(HalfDuplexRequest request, String action, boolean submit) {
        com.alibaba.dashscope.common.DashScopeResult response;
        try {
            response = client.send(request);
        } catch (ApiException ex) {
            throw convertException(ex, action, submit);
        } catch (NoApiKeyException ex) {
            // 构造时已校验 API Key 非空，请求没有发出
            throw new IllegalStateException(action + "失败：缺少 API Key");
        }
        try {
            return VideoSynthesisResult.fromDashScopeResult(response);
        } catch (RuntimeException ex) {
            // output、usage 结构不符合文档
            throw new AiVideoException(platform, AiVideoException.Kind.INVALID_RESPONSE, response.getStatusCode(), null,
                    response.getRequestId(), submit, action + "失败：返回格式不正确"
                    + (submit ? "，任务是否已创建未知，请勿自动重新提交" : ""));
        }
    }

    // ========== 异常 ==========

    private AiVideoException convertException(ApiException ex, String action, boolean submit) {
        Status status = ex.getStatus();
        int httpStatus = status != null ? status.getStatusCode() : -1;
        String unknownHint = "，任务是否已创建未知，请勿自动重新提交";
        // 1. 服务端返回了非 2xx：SDK 已解析出 code、message、request_id
        if (httpStatus > 0) {
            String code = StrUtil.emptyToNull(status.getCode());
            String requestId = StrUtil.emptyToNull(status.getRequestId());
            boolean unknown = submit && (httpStatus >= 500 || code == null);
            log.warn("[{}][{}失败，HTTP {}，code {}，requestId {}]", platform, action, httpStatus, code, requestId);
            return new AiVideoException(platform, getKind(code, httpStatus), httpStatus, code, requestId, unknown,
                    StrUtil.format("{}失败（HTTP {}，{}）：{}{}", action, httpStatus, StrUtil.blankToDefault(code, "-"),
                            AiVideoUtils.sanitize(StrUtil.blankToDefault(status.getMessage(), "返回格式不正确"), apiKey),
                            unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
        }
        // 2. 没有收到完整响应：超时、网络中断、响应过大
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof ResponseTooLargeException) {
            return new AiVideoException(platform, AiVideoException.Kind.INVALID_RESPONSE, null, null, null, submit,
                    action + "失败：响应超过大小上限" + (submit ? unknownHint : ""));
        }
        if (cause instanceof InterruptedIOException) {
            return new AiVideoException(platform, AiVideoException.Kind.TIMEOUT, null, null, null, submit,
                    action + "超时" + (submit ? unknownHint : ""));
        }
        // 连接被拒绝、域名解析失败：请求没有发出，可以确定任务未创建
        boolean notSent = cause instanceof ConnectException || cause instanceof UnknownHostException;
        return new AiVideoException(platform, AiVideoException.Kind.NETWORK, null, null, null, submit && !notSent,
                StrUtil.format("{}失败：网络异常（{}）{}", action, cause.getClass().getSimpleName(),
                        submit && !notSent ? unknownHint : ""));
    }

    static AiVideoException.Kind getKind(String code, int httpStatus) {
        if (code != null) {
            if (code.startsWith("InvalidApiKey")) {
                return AiVideoException.Kind.AUTHENTICATION;
            }
            if (code.startsWith("Arrearage")) {
                return AiVideoException.Kind.INSUFFICIENT_BALANCE;
            }
            if (code.startsWith("AccessDenied") || code.startsWith("Model.AccessDenied")
                    || code.startsWith("Workspace.AccessDenied")) {
                return AiVideoException.Kind.PERMISSION;
            }
            if (code.startsWith("Throttling")) {
                return AiVideoException.Kind.RATE_LIMITED;
            }
            if (code.startsWith("DataInspectionFailed") || code.contains("DataInspection")
                    || code.startsWith("IPInfringementSuspect")) {
                return AiVideoException.Kind.CONTENT_REJECTED;
            }
            if (code.startsWith("ModelNotFound") || code.startsWith("NotFound")) {
                return AiVideoException.Kind.NOT_FOUND;
            }
            if (code.startsWith("InvalidParameter") || code.startsWith("InvalidURL") || code.startsWith("BadRequest")) {
                return AiVideoException.Kind.INVALID_REQUEST;
            }
            if (code.startsWith("InternalError") || code.startsWith("SystemError")) {
                return AiVideoException.Kind.SERVER_ERROR;
            }
        }
        return AiVideoException.kindOfHttpStatus(httpStatus);
    }

    // ========== 传输 ==========

    /**
     * 本客户端独享的 OkHttpClient：不重试、不跟随重定向、有超时与响应上限、不记录网络日志
     */
    static OkHttpClient buildOkHttpClient(Duration timeout, long maxResponseSize) {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .protocols(Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .callTimeout(timeout)
                .addInterceptor(chain -> limitResponseSize(chain.proceed(chain.request()), maxResponseSize))
                // 网络拦截器位于 OkHttp 的重试与重定向拦截器之下：去掉 Retry-After，使其不会因「503 + Retry-After: 0」自动重发
                .addNetworkInterceptor(chain -> chain.proceed(chain.request()).newBuilder().removeHeader("Retry-After").build())
                .build();
    }

    /**
     * 最多缓冲 maxSize + 1 字节：超过上限直接报错，不会无界读取
     */
    private static Response limitResponseSize(Response response, long maxSize) throws IOException {
        ResponseBody body = response.body();
        if (body != null && body.source().request(maxSize + 1)) {
            response.close();
            throw new ResponseTooLargeException(maxSize);
        }
        return response;
    }

    /**
     * 响应体超过上限
     */
    static class ResponseTooLargeException extends IOException {

        ResponseTooLargeException(long maxSize) {
            super("响应超过 " + maxSize + " 字节");
        }

    }

}
