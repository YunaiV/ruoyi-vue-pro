package cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng;

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
import com.volcengine.auth.impl.SignerV4Impl;
import com.volcengine.model.Credentials;
import com.volcengine.model.RequestParam;
import com.volcengine.model.SignRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 即梦 Visual（火山引擎智能视觉）API：视频生成 3.0、数字人口播 OmniHuman（含主体识别、主体检测）、动作模仿 DreamActor
 *
 * 1. 鉴权二选一：
 *    a. AccessKey/SecretKey：POST https://visual.volcengineapi.com?Action={Action}&Version=2022-08-31，
 *       HMAC-SHA256 签名（Region cn-north-1，Service cv），复用官方 SDK（volc-sdk-java）的 {@link SignerV4Impl} 计算，
 *       见 {@link #sign}；请求仍由 {@link AiVideoHttpClient} 发送（不重试、不跟随重定向、响应有上限），
 *       不使用 SDK 的 VisualServiceImpl：其 Apache HttpClient 传输与 API Key 方式无法共用，且响应体不限大小
 *    b. IAM API Key：POST https://openapi.cv.volces.com/api/common/v3/{path}，Authorization: Bearer {API Key}；
 *       Action 与路径的对应见 {@link #API_KEY_PATHS}。注意：方舟（Ark）的 API Key 不能用于本接口
 * 2. 两类异步接口：
 *    a. CVSync2AsyncSubmitTask / CVSync2AsyncGetResult：视频生成 3.0、OmniHuman 2.0、动作模仿
 *    b. CVSubmitTask / CVGetResult：OmniHuman 1.5、主体识别
 *    查询的 Action 保存在 {@link AiVideoTaskRef#getRoute()}，req_key 保存在 {@link AiVideoTaskRef#getModel()}
 * 3. 响应：先判断 code（10000 为成功），再判断 data.status：in_queue、processing、generating、done、not_found、expired；
 *    video_url 有效期 1 小时
 * 4. OmniHuman 1.5 的流程：主体识别（必需，status=1 才能继续）→ 主体检测（可选，得到 mask 指定说话主体）→ 视频生成
 *
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/llms.txt">即梦 AI 文档索引</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/calling-step-1-subject-recognition.md">OmniHuman 1.5 主体识别</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/call-step-2-subject-detection.md">OmniHuman 1.5 主体检测</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/call-step3-video-generation.md">OmniHuman 1.5 视频生成</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/omni-human20-video-generation-interface-documentation.md">OmniHuman 2.0</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/action-imitation-interface-documentation.md">动作模仿</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/action-imitation-20-interface-documentation.md">动作模仿 2.0</a>
 * @see <a href="https://docs.volcengine.com/docs/zh/jimeng-ai/jimeng-ai-video-generation-30-pro-interface-documentation.md">视频生成 3.0 Pro（下线中）</a>
 * @see <a href="https://docs.volcengine.com/docs/AIGCAIzhongtaigongyongwendang/api-key-usage-guide?lang=zh">API Key 调用方式</a>
 * @author 芋道源码
 */
@Slf4j
public class JimengVisualApi {

    public static final String PLATFORM = "jimeng";
    public static final String DEFAULT_SIGN_BASE_URL = "https://visual.volcengineapi.com";
    public static final String DEFAULT_API_KEY_BASE_URL = "https://openapi.cv.volces.com";
    public static final String REGION = "cn-north-1";
    public static final String SERVICE = "cv";
    public static final String VERSION = "2022-08-31";

    public static final String ACTION_SYNC2ASYNC_SUBMIT = "CVSync2AsyncSubmitTask";
    public static final String ACTION_SYNC2ASYNC_GET_RESULT = "CVSync2AsyncGetResult";
    public static final String ACTION_SUBMIT = "CVSubmitTask";
    public static final String ACTION_GET_RESULT = "CVGetResult";
    public static final String ACTION_PROCESS = "CVProcess";

    /**
     * API Key 方式：Action → 路径
     */
    public static final Map<String, String> API_KEY_PATHS = new LinkedHashMap<>();

    static {
        API_KEY_PATHS.put(ACTION_SYNC2ASYNC_SUBMIT, "/api/common/v3/submit_task_v2");
        API_KEY_PATHS.put(ACTION_SYNC2ASYNC_GET_RESULT, "/api/common/v3/get_result_v2");
        API_KEY_PATHS.put(ACTION_SUBMIT, "/api/common/v3/submit_task");
        API_KEY_PATHS.put(ACTION_GET_RESULT, "/api/common/v3/get_result");
        API_KEY_PATHS.put(ACTION_PROCESS, "/api/common/v3/process");
    }

    // ========== req_key ==========

    public static final String REQ_KEY_T2V_V30 = "jimeng_t2v_v30";
    public static final String REQ_KEY_I2V_FIRST_V30 = "jimeng_i2v_first_v30";
    public static final String REQ_KEY_I2V_FIRST_V30_1080 = "jimeng_i2v_first_v30_1080";
    public static final String REQ_KEY_I2V_FIRST_TAIL_V30 = "jimeng_i2v_first_tail_v30";
    public static final String REQ_KEY_TI2V_V30_PRO = "jimeng_ti2v_v30_pro";
    /**
     * 视频生成 req_key → [最少图片数, 最多图片数, 是否支持 aspect_ratio]
     */
    public static final Map<String, int[]> VIDEO_REQ_KEYS = new LinkedHashMap<>();

    static {
        VIDEO_REQ_KEYS.put(REQ_KEY_T2V_V30, new int[]{0, 0, 1});
        VIDEO_REQ_KEYS.put(REQ_KEY_I2V_FIRST_V30, new int[]{1, 1, 0});
        VIDEO_REQ_KEYS.put(REQ_KEY_I2V_FIRST_V30_1080, new int[]{1, 1, 0});
        VIDEO_REQ_KEYS.put(REQ_KEY_I2V_FIRST_TAIL_V30, new int[]{2, 2, 0});
        VIDEO_REQ_KEYS.put(REQ_KEY_TI2V_V30_PRO, new int[]{0, 1, 1});
    }

    public static final String REQ_KEY_OMNI_HUMAN_V15 = "jimeng_realman_avatar_picture_omni_v15";
    public static final String REQ_KEY_OMNI_HUMAN_V15_SUBJECT_RECOGNITION = "jimeng_realman_avatar_picture_create_role_omni_v15";
    public static final String REQ_KEY_OMNI_HUMAN_SUBJECT_DETECTION = "jimeng_realman_avatar_object_detection";
    public static final String REQ_KEY_OMNI_HUMAN_V20 = "realman_avatar_picture_omni20_cvtob";
    public static final String REQ_KEY_DREAM_ACTOR_M1 = "jimeng_dream_actor_m1_gen_video_cv";
    public static final String REQ_KEY_DREAM_ACTOR_M20 = "jimeng_dreamactor_m20_gen_video";

    public static final List<String> ASPECT_RATIOS = ListUtil.of("16:9", "4:3", "1:1", "3:4", "9:16", "21:9");
    public static final List<Integer> FRAMES = ListUtil.of(121, 241);

    private static final int CODE_SUCCESS = 10000;
    private static final int MAX_PROMPT_LENGTH = 800;

    /**
     * 主体识别结果
     */
    @Data
    public static class SubjectRecognitionResult {

        private AiVideoTaskStatus status;
        private String rawStatus;
        /**
         * 是否包含人、类人、拟人等主体：未完成时为 null
         */
        private Boolean containsSubject;
        private String errorCode;
        private String errorMessage;

    }

    private static final String CONTENT_TYPE = "application/json";
    /**
     * 官方 SDK 的签名器：无状态，只计算签名
     */
    private static final SignerV4Impl SIGNER = new SignerV4Impl();

    private final String baseUrl;
    /**
     * AK/SK 方式的 AccessKeyId、SecretAccessKey；API Key 方式为 null
     */
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String apiKey;
    private final String[] secrets;
    private final Clock clock;
    private final AiVideoHttpClient httpClient;

    private JimengVisualApi(String baseUrl, String accessKeyId, String secretAccessKey, String apiKey, String[] secrets,
                            Clock clock, Duration timeout) {
        this.baseUrl = StrUtil.removeSuffix(baseUrl, "/");
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.apiKey = apiKey;
        this.secrets = secrets;
        this.clock = clock;
        this.httpClient = new AiVideoHttpClient(PLATFORM, timeout, AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE, secrets);
    }

    /**
     * AccessKey/SecretKey 签名方式
     *
     * @param baseUrl 地址，为空时为 https://visual.volcengineapi.com
     */
    public static JimengVisualApi ofAccessKey(String accessKeyId, String secretAccessKey, String baseUrl, Duration timeout) {
        return ofAccessKey(accessKeyId, secretAccessKey, baseUrl, timeout, Clock.systemUTC());
    }

    static JimengVisualApi ofAccessKey(String accessKeyId, String secretAccessKey, String baseUrl, Duration timeout, Clock clock) {
        if (StrUtil.isBlank(accessKeyId) || StrUtil.isBlank(secretAccessKey)) {
            throw new IllegalArgumentException("火山引擎的 AccessKeyId、SecretAccessKey 不能为空");
        }
        return new JimengVisualApi(StrUtil.blankToDefault(baseUrl, DEFAULT_SIGN_BASE_URL), accessKeyId.trim(),
                secretAccessKey.trim(), null, new String[]{accessKeyId, secretAccessKey}, clock, timeout);
    }

    /**
     * IAM API Key 方式（不是方舟 Ark 的 API Key）
     *
     * @param baseUrl 地址，为空时为 https://openapi.cv.volces.com
     */
    public static JimengVisualApi ofApiKey(String apiKey, String baseUrl, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("即梦的 API Key 不能为空");
        }
        return new JimengVisualApi(StrUtil.blankToDefault(baseUrl, DEFAULT_API_KEY_BASE_URL), null, null, apiKey.trim(),
                new String[]{apiKey}, Clock.systemUTC(), timeout);
    }

    // ========== 视频生成 3.0 ==========

    /**
     * 校验并构建视频生成 3.0 的请求体：不发请求
     */
    public static ObjectNode buildVideoRequestBody(JimengVideoRequest request) {
        String reqKey = AiVideoUtils.requireText(request.getReqKey(), "req_key");
        int[] spec = VIDEO_REQ_KEYS.get(reqKey);
        AiVideoUtils.checkArgument(spec != null, "即梦视频生成不支持 req_key({})，可选：{}", reqKey, VIDEO_REQ_KEYS.keySet());
        int urlCount = CollUtil.size(request.getImageUrls());
        int base64Count = CollUtil.size(request.getBinaryDataBase64());
        AiVideoUtils.checkArgument(urlCount == 0 || base64Count == 0, "image_urls 与 binary_data_base64 只能二选一");
        int imageCount = urlCount + base64Count;
        AiVideoUtils.checkArgument(imageCount >= spec[0] && imageCount <= spec[1], "req_key({})需要 {} ~ {} 张图片，实际 {} 张",
                reqKey, spec[0], spec[1], imageCount);
        // 文生必须有提示词；图生（3.0 Pro 除外）文档也要求提示词
        if (imageCount == 0 || !REQ_KEY_TI2V_V30_PRO.equals(reqKey)) {
            AiVideoUtils.requireText(request.getPrompt(), "提示词");
        }
        AiVideoUtils.checkMaxLength(request.getPrompt(), MAX_PROMPT_LENGTH, "提示词");
        AiVideoUtils.checkIn(request.getFrames(), FRAMES, "帧数");
        AiVideoUtils.checkIn(request.getAspectRatio(), ASPECT_RATIOS, "比例");
        AiVideoUtils.checkArgument(request.getAspectRatio() == null || (spec[2] == 1 && imageCount == 0),
                "只有文生视频支持 aspect_ratio");
        AiVideoUtils.checkRange(request.getSeed(), -1, 4294967295L, "随机种子");

        ObjectNode body = AiVideoUtils.newObject();
        body.put("req_key", reqKey);
        if (request.getPrompt() != null) {
            body.put("prompt", request.getPrompt());
        }
        if (urlCount > 0) {
            ArrayNode urls = body.putArray("image_urls");
            for (String url : request.getImageUrls()) {
                urls.add(AiVideoUtils.checkMediaUrl(url, "图片地址"));
            }
        }
        if (base64Count > 0) {
            ArrayNode images = body.putArray("binary_data_base64");
            for (String image : request.getBinaryDataBase64()) {
                AiVideoUtils.checkArgument(StrUtil.isNotBlank(image) && !image.startsWith("data:"),
                        "binary_data_base64 不能为空，且不能带 data: 前缀");
                images.add(image);
            }
        }
        if (request.getSeed() != null) {
            body.put("seed", request.getSeed());
        }
        if (request.getFrames() != null) {
            body.put("frames", request.getFrames());
        }
        if (request.getAspectRatio() != null) {
            body.put("aspect_ratio", request.getAspectRatio());
        }
        return body;
    }

    /**
     * 提交视频生成 3.0 任务：只请求一次，不重试
     */
    public AiVideoTaskRef submitVideo(JimengVideoRequest request) {
        ObjectNode body = buildVideoRequestBody(request);
        return submit(request.getReqKey(), ACTION_SYNC2ASYNC_SUBMIT, ACTION_SYNC2ASYNC_GET_RESULT, body, "提交即梦视频任务");
    }

    // ========== 数字人口播 OmniHuman ==========

    /**
     * 提交 OmniHuman 1.5 主体识别任务（CVSubmitTask）：判断图片中是否包含人、类人、拟人等主体
     */
    public AiVideoTaskRef submitSubjectRecognition(String imageUrl) {
        ObjectNode body = AiVideoUtils.newObject();
        body.put("req_key", REQ_KEY_OMNI_HUMAN_V15_SUBJECT_RECOGNITION);
        body.put("image_url", AiVideoUtils.checkMediaUrl(imageUrl, "人物图片地址"));
        return submit(REQ_KEY_OMNI_HUMAN_V15_SUBJECT_RECOGNITION, ACTION_SUBMIT, ACTION_GET_RESULT, body, "提交即梦主体识别");
    }

    /**
     * 查询主体识别结果：resp_data.status 为 1 表示包含主体
     */
    public SubjectRecognitionResult getSubjectRecognition(AiVideoTaskRef ref) {
        AiVideoUtils.checkArgument(ref != null && REQ_KEY_OMNI_HUMAN_V15_SUBJECT_RECOGNITION.equals(ref.getModel()),
                "不是主体识别任务");
        JsonNode root = query(ref, "查询即梦主体识别");
        SubjectRecognitionResult result = new SubjectRecognitionResult();
        Integer code = AiVideoUtils.integer(root, "code");
        if (code == null || code != CODE_SUCCESS) {
            result.setStatus(AiVideoTaskStatus.FAILED);
            result.setErrorCode(code != null ? String.valueOf(code) : null);
            result.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(root, "message"), secrets));
            return result;
        }
        JsonNode data = root.get("data");
        String status = AiVideoUtils.text(data, "status");
        result.setRawStatus(status);
        result.setStatus(mapStatus(status));
        if ("done".equals(status)) {
            JsonNode respData = AiVideoUtils.parseJson(AiVideoUtils.text(data, "resp_data"));
            Integer subject = AiVideoUtils.integer(respData, "status");
            if (subject == null) {
                result.setStatus(AiVideoTaskStatus.FAILED);
                result.setErrorMessage("主体识别结果格式不正确");
            } else {
                result.setStatus(AiVideoTaskStatus.SUCCEEDED);
                result.setContainsSubject(subject == 1);
            }
        }
        return result;
    }

    /**
     * 主体检测（CVProcess，同步）：返回各主体的 mask 图地址（有效期 1 小时，按面积从大到小），没有主体时为空列表
     */
    public List<String> detectSubjects(String imageUrl) {
        ObjectNode body = AiVideoUtils.newObject();
        body.put("req_key", REQ_KEY_OMNI_HUMAN_SUBJECT_DETECTION);
        body.put("image_url", AiVideoUtils.checkMediaUrl(imageUrl, "人物图片地址"));
        AiVideoHttpClient.HttpResult result = send(ACTION_PROCESS, body, "即梦主体检测", false);
        JsonNode root = checkResponse(result, "即梦主体检测", false);
        JsonNode respData = AiVideoUtils.parseJson(AiVideoUtils.text(root.get("data"), "resp_data"));
        if (respData == null) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null, null, false,
                    "即梦主体检测失败：resp_data 格式不正确");
        }
        Integer innerCode = AiVideoUtils.integer(respData, "code");
        if (innerCode != null && innerCode != 0) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_REQUEST, result.getStatus(), String.valueOf(innerCode),
                    null, false, "即梦主体检测失败：resp_data.code=" + innerCode);
        }
        if (!Integer.valueOf(1).equals(AiVideoUtils.integer(respData, "status"))) {
            return Collections.emptyList();
        }
        List<String> masks = new ArrayList<>();
        JsonNode urls = respData.path("object_detection_result").path("mask").get("url");
        if (urls != null && urls.isArray()) {
            for (JsonNode url : urls) {
                masks.add(url.asText());
            }
        }
        return masks;
    }

    /**
     * 校验并构建数字人口播的请求体：不发请求
     */
    public static ObjectNode buildOmniHumanRequestBody(JimengOmniHumanRequest request) {
        String reqKey = AiVideoUtils.requireText(request.getReqKey(), "req_key");
        boolean v15 = REQ_KEY_OMNI_HUMAN_V15.equals(reqKey);
        boolean v20 = REQ_KEY_OMNI_HUMAN_V20.equals(reqKey);
        AiVideoUtils.checkArgument(v15 || v20, "即梦数字人不支持 req_key({})，可选：{}、{}", reqKey,
                REQ_KEY_OMNI_HUMAN_V15, REQ_KEY_OMNI_HUMAN_V20);
        ObjectNode body = AiVideoUtils.newObject();
        body.put("req_key", reqKey);
        String imageUrl = AiVideoUtils.checkMediaUrl(request.getImageUrl(), "人物图片地址");
        if (v15) {
            body.put("image_url", imageUrl);
        } else {
            body.putArray("image_urls").add(imageUrl);
        }
        body.put("audio_url", AiVideoUtils.checkMediaUrl(request.getAudioUrl(), "驱动音频地址"));
        AiVideoUtils.checkMaxLength(request.getPrompt(), v15 ? 1000 : 2000, "提示词");
        if (request.getPrompt() != null) {
            body.put("prompt", request.getPrompt());
        }
        if (v15) {
            if (CollUtil.isNotEmpty(request.getMaskUrls())) {
                ArrayNode masks = body.putArray("mask_url");
                for (String mask : request.getMaskUrls()) {
                    masks.add(AiVideoUtils.checkMediaUrl(mask, "mask 图地址"));
                }
            }
            if (request.getSeed() != null) {
                body.put("seed", AiVideoUtils.checkRange(request.getSeed(), -1, 4294967295L, "随机种子"));
            }
            if (request.getOutputResolution() != null) {
                body.put("output_resolution", AiVideoUtils.checkIn(request.getOutputResolution(), ListUtil.of(720, 1080), "输出分辨率"));
            }
            if (request.getPeFastMode() != null) {
                body.put("pe_fast_mode", request.getPeFastMode());
            }
            AiVideoUtils.checkArgument(request.getEnableWatermark() == null, "OmniHuman 1.5 不支持 enable_watermark");
        } else {
            AiVideoUtils.checkArgument(CollUtil.isEmpty(request.getMaskUrls()) && request.getSeed() == null
                            && request.getOutputResolution() == null && request.getPeFastMode() == null,
                    "OmniHuman 2.0 不支持 mask_url、seed、output_resolution、pe_fast_mode");
            if (request.getEnableWatermark() != null) {
                body.put("enable_watermark", request.getEnableWatermark());
            }
        }
        return body;
    }

    /**
     * 提交数字人口播任务：只请求一次，不重试
     *
     * 注意：OmniHuman 1.5 需要调用方先完成主体识别（{@link #submitSubjectRecognition}、{@link #getSubjectRecognition}），
     * 识别结果包含主体时再提交，避免无效计费
     */
    public AiVideoTaskRef submitOmniHuman(JimengOmniHumanRequest request) {
        ObjectNode body = buildOmniHumanRequestBody(request);
        boolean v15 = REQ_KEY_OMNI_HUMAN_V15.equals(request.getReqKey());
        return submit(request.getReqKey(), v15 ? ACTION_SUBMIT : ACTION_SYNC2ASYNC_SUBMIT,
                v15 ? ACTION_GET_RESULT : ACTION_SYNC2ASYNC_GET_RESULT, body, "提交即梦数字人任务");
    }

    // ========== 动作模仿 DreamActor ==========

    /**
     * 校验并构建动作模仿的请求体：不发请求
     */
    public static ObjectNode buildDreamActorRequestBody(JimengDreamActorRequest request) {
        String reqKey = AiVideoUtils.requireText(request.getReqKey(), "req_key");
        boolean m1 = REQ_KEY_DREAM_ACTOR_M1.equals(reqKey);
        boolean m20 = REQ_KEY_DREAM_ACTOR_M20.equals(reqKey);
        AiVideoUtils.checkArgument(m1 || m20, "即梦动作模仿不支持 req_key({})，可选：{}、{}", reqKey,
                REQ_KEY_DREAM_ACTOR_M1, REQ_KEY_DREAM_ACTOR_M20);
        ObjectNode body = AiVideoUtils.newObject();
        body.put("req_key", reqKey);
        String imageUrl = AiVideoUtils.checkMediaUrl(request.getImageUrl(), "人物图片地址");
        if (m1) {
            body.put("image_url", imageUrl);
        } else {
            body.putArray("image_urls").add(imageUrl);
        }
        body.put("video_url", AiVideoUtils.checkMediaUrl(request.getVideoUrl(), "动作视频地址"));
        if (m1) {
            AiVideoUtils.checkArgument(request.getCutResultFirstSecondSwitch() == null, "动作模仿 M1 不支持 cut_result_first_second_switch");
            if (request.getCallbackUrl() != null) {
                body.put("callback_url", AiVideoUtils.checkMediaUrl(request.getCallbackUrl(), "回调地址"));
            }
        } else {
            AiVideoUtils.checkArgument(request.getCallbackUrl() == null, "动作模仿 2.0 文档未提供 callback_url");
            if (request.getCutResultFirstSecondSwitch() != null) {
                body.put("cut_result_first_second_switch", request.getCutResultFirstSecondSwitch());
            }
        }
        return body;
    }

    /**
     * 提交动作模仿任务：只请求一次，不重试
     */
    public AiVideoTaskRef submitDreamActor(JimengDreamActorRequest request) {
        ObjectNode body = buildDreamActorRequestBody(request);
        return submit(request.getReqKey(), ACTION_SYNC2ASYNC_SUBMIT, ACTION_SYNC2ASYNC_GET_RESULT, body, "提交即梦动作模仿任务");
    }

    // ========== 查询 ==========

    /**
     * 查询视频类任务（视频生成、数字人、动作模仿）：只查询一次
     */
    public AiVideoTaskResult getTask(AiVideoTaskRef ref) {
        JsonNode root = query(ref, "查询即梦任务");
        AiVideoTaskResult task = new AiVideoTaskResult();
        task.setTaskId(ref.getTaskId());
        task.setRequestId(AiVideoUtils.text(root, "request_id"));
        Integer code = AiVideoUtils.integer(root, "code");
        if (code == null || code != CODE_SUCCESS) {
            // 任务处理失败（审核未通过、素材不符合要求等）：code 非 10000，data 为空
            task.setStatus(AiVideoTaskStatus.FAILED);
            task.setErrorCode(code != null ? String.valueOf(code) : null);
            task.setErrorMessage(AiVideoUtils.sanitize(AiVideoUtils.text(root, "message"), secrets));
            return task;
        }
        JsonNode data = root.get("data");
        String status = AiVideoUtils.text(data, "status");
        task.setRawStatus(status);
        task.putMetadata("aigcMetaTagged", AiVideoUtils.text(data, "aigc_meta_tagged"));
        if ("done".equals(status)) {
            String url = AiVideoUtils.text(data, "video_url");
            if (url != null) {
                AiVideoOutput output = new AiVideoOutput();
                output.setUrl(url);
                output.setMimeType("video/mp4");
                JsonNode respData = AiVideoUtils.parseJson(AiVideoUtils.text(data, "resp_data"));
                if (respData != null) {
                    output.setDurationSeconds(AiVideoUtils.decimal(respData.path("video_meta"), "Duration"));
                }
                task.getVideos().add(output);
            }
            return task.succeedOrFailIfEmpty();
        }
        task.setStatus(mapStatus(status));
        return task;
    }

    private static AiVideoTaskStatus mapStatus(String status) {
        if ("in_queue".equals(status)) {
            return AiVideoTaskStatus.QUEUED;
        }
        if ("processing".equals(status) || "generating".equals(status)) {
            return AiVideoTaskStatus.RUNNING;
        }
        if ("expired".equals(status)) {
            return AiVideoTaskStatus.EXPIRED;
        }
        // not_found：无此任务或已超过 12 小时；与未识别的状态一样按未知处理
        return AiVideoTaskStatus.UNKNOWN;
    }

    /**
     * 查询：鉴权、限流、服务端错误抛出异常（可稍后重查）；业务失败（审核、素材等）返回 code 由调用方判断
     */
    private JsonNode query(AiVideoTaskRef ref, String action) {
        AiVideoUtils.checkArgument(ref != null, "任务引用不能为空");
        String reqKey = AiVideoUtils.requireText(ref.getModel(), "req_key");
        String taskId = AiVideoUtils.requireText(ref.getTaskId(), "任务编号");
        AiVideoUtils.checkArgument(taskId.matches("[A-Za-z0-9_\\-]{1,128}"), "任务编号格式不正确");
        String queryAction = ref.getRoute();
        AiVideoUtils.checkArgument(ACTION_GET_RESULT.equals(queryAction) || ACTION_SYNC2ASYNC_GET_RESULT.equals(queryAction),
                "未知的即梦查询 Action({})", queryAction);
        ObjectNode body = AiVideoUtils.newObject();
        body.put("req_key", reqKey);
        body.put("task_id", taskId);
        AiVideoHttpClient.HttpResult result = send(queryAction, body, action, false);
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        Integer code = AiVideoUtils.integer(root, "code");
        if (code != null && code != CODE_SUCCESS && isTaskFailure(code)) {
            return root;
        }
        return checkResponse(result, action, false);
    }

    private AiVideoTaskRef submit(String reqKey, String submitAction, String queryAction, ObjectNode body, String action) {
        AiVideoHttpClient.HttpResult result = send(submitAction, body, action, true);
        JsonNode root = checkResponse(result, action, true);
        String taskId = AiVideoUtils.text(root.get("data"), "task_id");
        if (taskId == null) {
            throw new AiVideoException(PLATFORM, AiVideoException.Kind.INVALID_RESPONSE, result.getStatus(), null,
                    AiVideoUtils.text(root, "request_id"), true, action + "后未返回 task_id，任务是否已创建未知，请勿自动重新提交");
        }
        log.info("[submit][{}成功，reqKey({}) taskId({})]", action, reqKey, taskId);
        return new AiVideoTaskRef(PLATFORM, reqKey, taskId, queryAction, AiVideoUtils.text(root, "request_id"));
    }

    private AiVideoHttpClient.HttpResult send(String apiAction, ObjectNode body, String action, boolean submit) {
        byte[] bytes = AiVideoUtils.toJsonBytes(body);
        Map<String, String> headers;
        URI uri;
        if (accessKeyId != null) {
            URI base = URI.create(baseUrl);
            String host = base.getPort() > 0 ? base.getHost() + ":" + base.getPort() : base.getHost();
            String path = StrUtil.blankToDefault(base.getPath(), "/");
            headers = sign(accessKeyId, secretAccessKey, host, path, apiAction, bytes, clock.instant());
            // Action 只来自本类的常量，Version 固定，无需编码；Host 由 HTTP 客户端按地址设置
            uri = URI.create(baseUrl + (base.getPath().isEmpty() ? "/" : "") + "?Action=" + apiAction + "&Version=" + VERSION);
        } else {
            headers = new LinkedHashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            headers.put("Content-Type", CONTENT_TYPE);
            uri = URI.create(baseUrl + API_KEY_PATHS.get(apiAction));
        }
        return httpClient.execute(HttpMethod.POST, uri, headers, bytes, action, submit);
    }

    /**
     * 用官方 SDK 的 {@link SignerV4Impl#getSignRequest} 计算 AK/SK 签名（只计算，不发请求），返回需要附加的请求头：
     * Content-Type、X-Date、X-Content-Sha256、Authorization
     *
     * {@link Credentials} 的 toString 含密钥，因此每次在方法内临时创建，不保存为字段、不记录日志
     */
    static Map<String, String> sign(String accessKeyId, String secretAccessKey, String host, String path,
                                    String apiAction, byte[] body, Instant now) {
        Credentials credentials = new Credentials(REGION, SERVICE);
        credentials.setAccessKeyID(accessKeyId);
        credentials.setSecretAccessKey(secretAccessKey);
        // getSignRequest 会对 queryList 排序，每次新建
        List<NameValuePair> query = new ArrayList<>();
        query.add(new BasicNameValuePair("Action", apiAction));
        query.add(new BasicNameValuePair("Version", VERSION));
        RequestParam param = RequestParam.builder().isSignUrl(false).method("POST").host(host).path(path)
                .queryList(query).headers(new Header[]{new BasicHeader("Content-Type", CONTENT_TYPE)})
                .body(body != null ? body : new byte[0]).date(Date.from(now)).build();
        SignRequest signRequest;
        try {
            signRequest = SIGNER.getSignRequest(param, credentials);
        } catch (Exception ex) {
            // 不带原始消息，避免泄露
            throw new IllegalStateException("火山引擎签名失败：" + ex.getClass().getSimpleName());
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", CONTENT_TYPE);
        headers.put("X-Date", signRequest.getXDate());
        headers.put("X-Content-Sha256", signRequest.getXContentSha256());
        headers.put("Authorization", signRequest.getAuthorization());
        return headers;
    }

    private JsonNode checkResponse(AiVideoHttpClient.HttpResult result, String action, boolean submit) {
        JsonNode root = AiVideoUtils.parseJson(result.getBody());
        Integer code = AiVideoUtils.integer(root, "code");
        if (result.is2xx() && code != null && code == CODE_SUCCESS) {
            return root;
        }
        String message = AiVideoUtils.text(root, "message");
        String codeText = code != null ? String.valueOf(code) : null;
        String requestId = AiVideoUtils.text(root, "request_id");
        AiVideoException.Kind kind;
        if (code != null) {
            kind = getKind(code, message, result.getStatus());
        } else {
            // 网关层错误：{ResponseMetadata: {RequestId, Error: {Code, Message}}}
            JsonNode metadata = root != null ? root.get("ResponseMetadata") : null;
            JsonNode error = metadata != null ? metadata.get("Error") : null;
            codeText = AiVideoUtils.text(error, "Code");
            message = AiVideoUtils.text(error, "Message");
            requestId = AiVideoUtils.text(metadata, "RequestId");
            kind = getGatewayKind(codeText, result.getStatus());
        }
        boolean unknown = submit && (result.getStatus() >= 500 || (code == null && codeText == null));
        log.warn("[Jimeng][{}失败，HTTP {}，code {}，requestId {}]", action, result.getStatus(), codeText, requestId);
        throw new AiVideoException(PLATFORM, kind, result.getStatus(), codeText, requestId, unknown,
                StrUtil.format("{}失败（HTTP {}，code={}）：{}{}", action, result.getStatus(), StrUtil.blankToDefault(codeText, "-"),
                        StrUtil.isBlank(message) ? "返回格式不正确" : AiVideoUtils.sanitize(message, secrets),
                        unknown ? "；任务是否已创建未知，请勿自动重新提交" : ""));
    }

    /**
     * 是否为任务本身的失败（审核未通过、输入不符合要求），而不是鉴权、限流、服务端错误
     */
    static boolean isTaskFailure(int code) {
        return getKind(code, null, 200) == AiVideoException.Kind.CONTENT_REJECTED
                || (code >= 50200 && code <= 50299);
    }

    /**
     * 按文档的错误码表判断；文档未列出的错误码按 HTTP 状态判断
     */
    static AiVideoException.Kind getKind(int code, String message, int httpStatus) {
        if (code == 50400) {
            // 例如「apikey does not exist」、签名错误；其他为无权限访问
            return StrUtil.containsAnyIgnoreCase(message, "apikey", "signature", "accesskey", "credential")
                    ? AiVideoException.Kind.AUTHENTICATION : AiVideoException.Kind.PERMISSION;
        }
        if (code == 50429 || code == 50430) {
            return AiVideoException.Kind.RATE_LIMITED;
        }
        if (code == 50500 || code == 50501 || code == 50520 || code == 50521 || code == 50522) {
            return AiVideoException.Kind.SERVER_ERROR;
        }
        if ((code >= 50411 && code <= 50413) || (code >= 50511 && code <= 50519)) {
            return AiVideoException.Kind.CONTENT_REJECTED;
        }
        if (code >= 50200 && code <= 50299) {
            // 50215 Input invalid for this service 等
            return AiVideoException.Kind.INVALID_REQUEST;
        }
        return AiVideoException.kindOfHttpStatus(httpStatus);
    }

    static AiVideoException.Kind getGatewayKind(String code, int httpStatus) {
        if (code != null) {
            if (code.contains("Signature") || code.contains("AccessKey") || code.contains("Authentication")
                    || code.contains("Credential")) {
                return AiVideoException.Kind.AUTHENTICATION;
            }
            if (code.contains("AccessDenied") || code.contains("Forbidden") || code.contains("NotOpen")) {
                return AiVideoException.Kind.PERMISSION;
            }
            if (code.contains("FlowLimit") || code.contains("Throttl")) {
                return AiVideoException.Kind.RATE_LIMITED;
            }
            if (code.contains("InternalError") || code.contains("ServiceUnavailable")) {
                return AiVideoException.Kind.SERVER_ERROR;
            }
        }
        return AiVideoException.kindOfHttpStatus(httpStatus);
    }

}
