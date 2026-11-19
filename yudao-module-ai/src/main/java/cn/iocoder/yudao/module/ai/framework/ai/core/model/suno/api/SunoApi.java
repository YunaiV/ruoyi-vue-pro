package cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Suno API
 *
 * 对接 New API 兼容的 /suno/* 中转协议，Bearer 鉴权；只轮询，不使用 notify_hook 回调。
 *
 * 中转前提：
 * 1. 生成、续写（/suno/submit/music）与查询（/suno/fetch）为基础能力
 * 2. 拼接（/suno/submit/concat）、上传（/suno/uploads/audio-url）为扩展路径：New API 官方 SunoAPI 插件 1.1.0
 *    只接受 /suno/submit/{music|lyrics}，不支持这两项；需使用实现了对应路径的兼容中转，否则提交即失败
 * 3. 例外：根地址为 apimax 官方 API（{@link #APIMAX_BASE_URL}）时，灵感、自定义的生成改走其原生接口 /v1/music/generations
 *    （其 /suno/submit/music 会返回 400），续写、拼接、上传、查询仍走上述路径
 *
 * @see <a href="https://doc.newapi.pro/api/suno-music/">New API · Suno 音乐接口</a>
 * @see <a href="https://apimax.ai/zh/docs/suno-submit-music">apimax · Suno 音乐生成</a>
 * @see <a href="https://github.com/QuantumNous/new-api-plugins/blob/main/plugins/tasks/sunoapi/1.1.0/plugin.js">New API 官方 SunoAPI 插件</a>
 * @author xiaoxin
 */
@Slf4j
public class SunoApi {

    /**
     * 单次请求的超时时间：提交、查询都是同步等待，避免中转无响应时阻塞请求线程或同步任务
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    /**
     * 中转返回成功的 code
     */
    private static final String CODE_SUCCESS = "success";
    /**
     * 日志里保留的最大响应长度
     */
    private static final int MAX_LOG_LENGTH = 500;
    /**
     * 地址中的查询参数：上传时请求体、响应里可能带私有文件的签名地址，写日志前打码
     */
    private static final Pattern URL_QUERY_PATTERN = Pattern.compile("(https?://[^\\s?#\"'<>]+)\\?[^\\s\"'<>]*");
    /**
     * apimax 官方 API 根地址：整体精确匹配，避免误匹配相近域名或其它路径
     */
    private static final String APIMAX_BASE_URL = "https://api.apimax.ai";
    /**
     * 歌曲字段的其它写法（例如 InfAI）：key 为标准字段，value 为其它写法，参见 {@link #normalizeClip(ObjectNode)}
     */
    private static final Map<String, String> CLIP_FIELD_ALIASES = ImmutableMap.of(
            "audio_url", "cld2AudioUrl", "image_url", "cld2ImageUrl", "video_url", "cld2VideoUrl");
    /**
     * 歌曲元数据的字段：没有 metadata 对象时，从歌曲顶层读取，参见 {@link #normalizeClip(ObjectNode)}
     */
    private static final List<String> CLIP_METADATA_FIELDS = Arrays.asList("tags", "prompt", "duration");

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    /**
     * 日志只记录方法、路径、状态和限长的错误摘要，不记录请求体与完整地址
     */
    private final Function<ClientResponse, Mono<? extends Throwable>> EXCEPTION_FUNCTION =
            response -> response.bodyToMono(String.class).defaultIfEmpty("").handle((responseBody, sink) -> {
                HttpRequest request = response.request();
                HttpStatusCode status = response.statusCode();
                log.error("[suno-api] 调用失败！请求方式:[{}]，请求路径:[{}]，响应状态:[{}]，错误摘要:[{}]",
                        request.getMethod(), request.getURI().getPath(), status.value(), buildLogText(parseMessage(responseBody)));
                // 404、405 通常是中转未实现该路径（例如官方 SunoAPI 插件没有上传、拼接），只给出路径，不暴露中转地址
                if (status.isSameCodeAs(HttpStatus.NOT_FOUND) || status.isSameCodeAs(HttpStatus.METHOD_NOT_ALLOWED)) {
                    sink.error(new IllegalStateException(StrUtil.format("当前中转不支持该接口（HTTP {}）：{}",
                            status.value(), request.getURI().getPath())));
                    return;
                }
                sink.error(new IllegalStateException(StrUtil.format("中转请求失败（HTTP {}）：{}",
                        status.value(), parseMessage(responseBody))));
            });

    /**
     * 中转根地址：已去掉结尾的 /
     */
    private final String baseUrl;
    private final WebClient webClient;

    public SunoApi(String baseUrl, String apiKey) {
        this.baseUrl = StrUtil.removeSuffix(baseUrl, "/");
        this.webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    if (StrUtil.isNotEmpty(apiKey)) {
                        httpHeaders.setBearerAuth(apiKey);
                    }
                })
                .build();
    }

    /**
     * 提交音乐生成：灵感、自定义、续写共用
     *
     * apimax 官方 API 的灵感、自定义生成走原生接口；续写（带 taskId、continueClipId、continueAt 任一）仍走 /suno/submit/music
     *
     * @param request 请求
     * @return 中转任务编号
     */
    public String submitMusic(MusicRequest request) {
        boolean extend = request.taskId() != null || request.continueClipId() != null || request.continueAt() != null;
        if (isApimax() && !extend) {
            // 无歌词的纯音乐（自定义模式）：apimax 按 prompt 理解风格，只传 style 可能被忽略，所以没有描述时用风格作为描述
            String lyrics = StrUtil.emptyToNull(request.prompt());
            String prompt = lyrics == null && Boolean.TRUE.equals(request.makeInstrumental())
                    ? StrUtil.blankToDefault(request.gptDescriptionPrompt(), request.tags()) : request.gptDescriptionPrompt();
            return submit("/v1/music/generations", new ApimaxMusicRequest(request.mv(), prompt,
                    lyrics, request.tags(), request.title(), request.makeInstrumental()));
        }
        return submit("/suno/submit/music", request);
    }

    /**
     * 是否为 apimax 官方 API：是的话，普通生成走原生接口
     */
    private boolean isApimax() {
        return APIMAX_BASE_URL.equalsIgnoreCase(baseUrl);
    }

    /**
     * 提交拼接：把续写作品与原曲合并为完整歌曲
     *
     * 注意：扩展路径，需中转实现 /suno/submit/concat
     *
     * @param clipId 续写作品的歌曲编号
     * @return 中转任务编号
     */
    public String submitConcat(String clipId) {
        return submit("/suno/submit/concat", new ConcatRequest(clipId, false));
    }

    /**
     * 提交上传：中转拉取音频地址后生成可续写的歌曲
     *
     * 注意：扩展路径，需中转实现 /suno/uploads/audio-url
     *
     * @param url 音频地址，中转必须能访问
     * @return 中转任务编号
     */
    public String uploadAudioUrl(String url) {
        return submit("/suno/uploads/audio-url", ImmutableMap.of("url", url));
    }

    /**
     * 批量查询任务
     *
     * @param taskIds 中转任务编号数组
     * @return 任务数组；查不到的任务、格式不正确的任务不在其中
     */
    public List<Task> fetchTasks(Collection<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        JsonNode data = post("/suno/fetch", ImmutableMap.of("ids", taskIds, "action", "MUSIC"));
        if (data == null || data.isNull()) {
            return Collections.emptyList();
        }
        if (!data.isArray()) {
            throw new IllegalStateException("中转查询返回格式不正确：data 不是数组");
        }
        // 逐个解析：单个任务格式异常（例如 SUBMITTED 时 data 为对象）只跳过该任务，不影响同批其它任务
        List<Task> tasks = new ArrayList<>(data.size());
        for (JsonNode node : data) {
            Task task = parseTask(node);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * 解析单个任务：不是对象或缺少 task_id 时跳过
     *
     * @param node 任务节点
     * @return 任务；无法解析时返回 null
     */
    private static Task parseTask(JsonNode node) {
        String taskId = node != null && node.isObject() ? getText(node, "task_id") : null;
        if (StrUtil.isBlank(taskId)) {
            log.warn("[suno-api][parseTask][任务缺少 task_id，跳过：{}]", buildLogText(String.valueOf(node)));
            return null;
        }
        return new Task(taskId, getText(node, "action"), getText(node, "status"), getText(node, "fail_reason"),
                getText(node, "progress"), parseClips(taskId, node.get("data")));
    }

    /**
     * 解析任务的歌曲，无法解析的歌曲逐首跳过
     *
     * 1. data 为数组：即歌曲列表
     * 2. data 为对象：取其中的 clips（例如 SalpX，以歌曲编号为键的对象），同对象里的 sunoData 是同一批歌曲，不重复解析；
     *    没有 clips 的（例如未完成时的提交确认对象），视为没有歌曲
     */
    private static List<Clip> parseClips(String taskId, JsonNode data) {
        JsonNode clipNodes = data != null && data.isObject() ? data.get("clips") : data;
        if (clipNodes == null || !(clipNodes.isArray() || clipNodes.isObject())) {
            return Collections.emptyList();
        }
        List<Clip> clips = new ArrayList<>(clipNodes.size());
        for (JsonNode node : clipNodes) {
            if (node == null || !node.isObject()) {
                continue;
            }
            try {
                clips.add(JsonUtils.parseObject(normalizeClip((ObjectNode) node).toString(), Clip.class));
            } catch (Exception e) {
                log.warn("[suno-api][parseClips][task({}) 歌曲格式不正确，跳过]", taskId, e);
            }
        }
        return clips;
    }

    /**
     * 兼容歌曲字段的其它写法（例如 InfAI），转换为 {@link Clip} 的标准字段；已有标准字段时不覆盖
     *
     * 1. 歌曲编号为 clipId（续写时的 continue_clip_id），id 是中转自己的记录编号：有 clipId 时，用它作为 id
     * 2. 音频、封面、视频地址为 cld2AudioUrl、cld2ImageUrl、cld2VideoUrl
     * 3. 没有 metadata 对象，风格、歌词、时长在歌曲顶层
     */
    private static ObjectNode normalizeClip(ObjectNode node) {
        ObjectNode clip = node.deepCopy();
        String clipId = getText(clip, "clipId");
        if (StrUtil.isNotBlank(clipId)) {
            clip.put("id", clipId);
        }
        CLIP_FIELD_ALIASES.forEach((field, alias) -> {
            if (!clip.hasNonNull(field) && clip.hasNonNull(alias)) {
                clip.set(field, clip.get(alias));
            }
        });
        if (!clip.path("metadata").isObject()) {
            ObjectNode metadata = clip.objectNode();
            for (String field : CLIP_METADATA_FIELDS) {
                JsonNode value = clip.get(field);
                if (value != null && value.isValueNode() && !value.isNull()) {
                    metadata.set(field, value);
                }
            }
            if (metadata.size() > 0) {
                clip.set("metadata", metadata);
            }
        }
        return clip;
    }

    private static String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asText();
    }

    /**
     * 提交任务：生成、续写、拼接、上传共用
     *
     * @param uri 提交路径
     * @param body 请求体
     * @return 中转任务编号；中转未返回时抛出异常
     */
    private String submit(String uri, Object body) {
        JsonNode data = post(uri, body);
        // 注意：显式 null 为 NullNode，isValueNode() 为 true 且 asText() 返回 "null"，需排除
        String taskId = data != null && !data.isNull() && data.isValueNode() ? data.asText() : null;
        if (StrUtil.isBlank(taskId)) {
            throw new IllegalStateException("中转提交成功但未返回任务编号");
        }
        return taskId;
    }

    /**
     * 发起 POST 请求，校验统一返回 {code, message, data}，返回 data 节点
     */
    private JsonNode post(String uri, Object body) {
        String response = webClient.post()
                .uri(uri)
                .body(Mono.just(JsonUtils.toJsonString(body)), String.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION)
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .block();
        JsonNode root = StrUtil.isBlank(response) || !JsonUtils.isJsonObject(response) ? null : JsonUtils.parseTree(response);
        if (root == null) {
            log.error("[suno-api][post][uri({}) 响应不是 JSON 对象：{}]", uri, buildLogText(response));
            throw new IllegalStateException("中转返回格式不正确");
        }
        String code = root.path("code").asText(null);
        if (!CODE_SUCCESS.equalsIgnoreCase(code)) {
            String message = parseMessage(response);
            log.error("[suno-api][post][uri({}) 返回失败：{}]", uri, buildLogText(message));
            throw new IllegalStateException(message);
        }
        return root.get("data");
    }

    /**
     * 日志用的文本：地址的查询参数（可能含签名）打码，并限长
     */
    private static String buildLogText(String text) {
        return text == null ? null : StrUtil.maxLength(URL_QUERY_PATTERN.matcher(text).replaceAll("$1?***"), MAX_LOG_LENGTH);
    }

    /**
     * 从中转的错误响应中取出可读的原因：message 或 error.message，都没有时返回原文
     */
    private static String parseMessage(String responseBody) {
        if (StrUtil.isBlank(responseBody) || !JsonUtils.isJsonObject(responseBody)) {
            return StrUtil.blankToDefault(responseBody, "无响应内容");
        }
        JsonNode root = JsonUtils.parseTree(responseBody);
        String message = root.path("message").asText("");
        if (StrUtil.isBlank(message)) {
            message = root.path("error").path("message").asText("");
        }
        return StrUtil.blankToDefault(message, responseBody);
    }

    // ========== 请求与响应 ==========

    /**
     * 音乐生成请求
     *
     * 灵感模式传 gptDescriptionPrompt；自定义模式传 prompt（歌词）、tags、title；续写再传 taskId、continueClipId、continueAt
     *
     * @param gptDescriptionPrompt 歌曲描述（灵感模式）
     * @param prompt 歌词（自定义模式）
     * @param tags 音乐风格
     * @param title 歌曲名称
     * @param mv 模型标识，例如 chirp-crow
     * @param makeInstrumental 是否纯音乐
     * @param taskId 续写：来源歌曲的任务编号
     * @param continueClipId 续写：来源歌曲编号
     * @param continueAt 续写：起点（秒）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MusicRequest(@JsonProperty("gpt_description_prompt") String gptDescriptionPrompt,
                               String prompt,
                               String tags,
                               String title,
                               String mv,
                               @JsonProperty("make_instrumental") Boolean makeInstrumental,
                               @JsonProperty("task_id") String taskId,
                               @JsonProperty("continue_clip_id") String continueClipId,
                               @JsonProperty("continue_at") Integer continueAt) {
    }

    /**
     * apimax 原生的音乐生成请求
     *
     * 灵感模式传 prompt（歌曲描述）；自定义模式传 lyrics（歌词）、style、title
     *
     * @param model 模型标识，例如 suno_music
     * @param prompt 歌曲描述（灵感模式），对应 {@link MusicRequest#gptDescriptionPrompt()}；无歌词的纯音乐没有描述时，取音乐风格
     * @param lyrics 歌词（自定义模式），对应 {@link MusicRequest#prompt()}
     * @param style 音乐风格，对应 {@link MusicRequest#tags()}
     * @param title 歌曲名称
     * @param instrumental 是否纯音乐
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApimaxMusicRequest(String model,
                                     String prompt,
                                     String lyrics,
                                     String style,
                                     String title,
                                     Boolean instrumental) {
    }

    /**
     * 拼接请求
     *
     * @param clipId 续写作品的歌曲编号
     * @param isInfill 是否填充，固定 false
     */
    public record ConcatRequest(@JsonProperty("clip_id") String clipId,
                                @JsonProperty("is_infill") Boolean isInfill) {
    }

    /**
     * 任务
     *
     * @param taskId 任务编号
     * @param action 任务类型，例如 MUSIC
     * @param status 任务状态：SUCCESS、FAILURE / FAIL，其余（NOT_START、SUBMITTED、QUEUED、IN_PROGRESS 等）视为处理中
     * @param failReason 失败原因
     * @param progress 进度，例如 62%
     * @param data 歌曲数组，成功时通常为 2 首
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Task(@JsonProperty("task_id") String taskId,
                       String action,
                       String status,
                       @JsonProperty("fail_reason") String failReason,
                       String progress,
                       List<Clip> data) {

        @JsonIgnore
        public boolean isSuccess() {
            return "SUCCESS".equalsIgnoreCase(status);
        }

        @JsonIgnore
        public boolean isFailure() {
            return StrUtil.equalsAnyIgnoreCase(status, "FAILURE", "FAIL", "FAILED")
                    || StrUtil.isNotBlank(failReason);
        }

    }

    /**
     * 歌曲
     *
     * @param id 歌曲编号（clip id）
     * @param title 歌曲名称
     * @param status 歌曲状态，例如 complete、error
     * @param audioUrl 音频地址
     * @param imageUrl 封面地址
     * @param imageLargeUrl 大封面地址
     * @param videoUrl 视频地址，可能为空
     * @param modelName 模型标识
     * @param metadata 元数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Clip(String id,
                       String title,
                       String status,
                       @JsonProperty("audio_url") String audioUrl,
                       @JsonProperty("image_url") String imageUrl,
                       @JsonProperty("image_large_url") String imageLargeUrl,
                       @JsonProperty("video_url") String videoUrl,
                       @JsonProperty("model_name") String modelName,
                       Metadata metadata) {

        /**
         * @return 封面地址：优先大图
         */
        @JsonIgnore
        public String coverUrl() {
            return StrUtil.blankToDefault(imageLargeUrl, imageUrl);
        }

        /**
         * @return 单首失败原因；为空表示该首正常
         */
        @JsonIgnore
        public String errorMessage() {
            String message = metadata != null && metadata.errorMessage() != null
                    && !metadata.errorMessage().isNull() ? metadata.errorMessage().asText(null) : null;
            if (StrUtil.isNotBlank(message)) {
                return message;
            }
            return "error".equalsIgnoreCase(status) ? "歌曲生成失败" : null;
        }

    }

    /**
     * 歌曲元数据
     *
     * @param tags 音乐风格
     * @param prompt 歌词
     * @param duration 时长（秒）
     * @param errorMessage 错误信息，可能是字符串或 null
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(String tags,
                           String prompt,
                           Double duration,
                           @JsonProperty("error_message") JsonNode errorMessage) {
    }

}
