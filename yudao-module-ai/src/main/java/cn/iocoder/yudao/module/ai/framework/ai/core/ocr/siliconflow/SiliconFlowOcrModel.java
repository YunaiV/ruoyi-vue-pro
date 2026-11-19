package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.siliconflow;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowApiConstants;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.getText;

/**
 * 硅基流动 DeepSeek-OCR 的 {@link OcrModel} 实现（平台 SILICONFLOW）
 *
 * 走 OpenAI 兼容的 /v1/chat/completions：图片以 data URL（base64）放在 image_url 中。
 * 不复用 Spring AI 的 OpenAiApi：它也能拿到结构化错误（chatCompletionEntity + 自定义 ResponseErrorHandler），
 * 但单次 OCR 请求要额外组装 ChatCompletionRequest / MediaContent，并替换默认把响应体拼进消息的错误处理；
 * 这里直接用 RestClient 发一个请求体，错误统一走 {@link OcrUtils#convertRestClientException} 脱敏，更简单。
 * 官方文档：https://docs.siliconflow.cn/cn/userguide/capabilities/multimodal-vision
 *
 * @author 芋道源码
 */
public class SiliconFlowOcrModel implements OcrModel {

    public static final String PLATFORM = "SILICONFLOW";

    /**
     * 默认地址：复用现有 {@link SiliconFlowApiConstants#DEFAULT_BASE_URL}
     */
    public static final String DEFAULT_BASE_URL = SiliconFlowApiConstants.DEFAULT_BASE_URL;
    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    /**
     * 默认模型：DeepSeek-OCR；也可配置 PaddlePaddle/PaddleOCR-VL 等 OCR 模型
     */
    public static final String DEFAULT_MODEL = "deepseek-ai/DeepSeek-OCR";

    /**
     * 硅基流动推荐的 DeepSeek-OCR 官方提示词（General OCR）：只输出纯文本
     */
    static final String PROMPT = "Free OCR.";

    /**
     * 文档未列出具体的图片类型和大小限制，这里取常见图片类型，并按 10MB 的 data URL 保守限制
     */
    static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/bmp");
    static final long MAX_DATA_URL_LENGTH = 10L * 1024 * 1024;

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public SiliconFlowOcrModel(String apiKey, String url, String model) {
        this(apiKey, url, model, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    SiliconFlowOcrModel(String apiKey, String url, String model, Duration connectTimeout, Duration readTimeout) {
        // 1. 原始 API Key，不拆分
        if (StrUtil.isBlank(apiKey)) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CREDENTIAL, "API Key 不能为空");
        }
        this.apiKey = apiKey.trim();
        // 2. 只接受 OCR 专用模型（名称包含 ocr），避免误配成普通对话模型
        this.model = StrUtil.blankToDefault(StrUtil.trim(model), DEFAULT_MODEL);
        if (!this.model.toLowerCase(Locale.ROOT).contains("ocr")) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG, "模型 " + this.model + " 不是 OCR 模型");
        }
        this.restClient = OcrUtils.buildRestClient(StrUtil.blankToDefault(url, DEFAULT_BASE_URL),
                connectTimeout, readTimeout);
    }

    @Override
    public OcrResponse call(OcrPrompt prompt) {
        // 1. 校验图片，转 data URL（仅内存中）
        byte[] image = OcrUtils.readImage(PLATFORM, prompt, ALLOWED_MIME_TYPES);
        String mimeType = OcrUtils.normalizeMimeType(prompt.getInstructions().getMimeType());
        String dataUrl = "data:" + mimeType + ";base64," + Base64.encode(image);
        OcrUtils.checkSize(PLATFORM, dataUrl.length(), MAX_DATA_URL_LENGTH, "base64 编码后的图片");

        // 2. 调用
        Map<String, Object> request = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)),
                                Map.of("type", "text", "text", PROMPT)))));
        String body;
        try {
            body = restClient.post().uri(CHAT_COMPLETIONS_PATH)
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw OcrUtils.convertRestClientException(PLATFORM, ex, apiKey);
        }

        // 3. 解析：choices[0].message.content
        return new OcrResponse(new OcrResult(parseContent(body)));
    }

    private static String parseContent(String body) {
        // 不用 JsonUtils.parseTree：解析失败时它会把原文（识别结果）打进日志
        JsonNode root = JsonUtils.parseObjectQuietly(body, JsonNode.class);
        if (root == null) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, "响应不是合法的 JSON");
        }
        // 硅基流动的业务错误体：{"code":20015,"message":"...","data":null}
        JsonNode choices = root.get("choices");
        if (choices == null && root.get("code") != null) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, getText(root, "code"),
                    null, OcrUtils.sanitize(getText(root, "message")));
        }
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, null,
                    getText(root, "id"), "响应中没有 choices");
        }
        JsonNode content = choices.get(0).path("message").path("content");
        if (content.isTextual()) {
            return OcrUtils.normalizeText(content.asText());
        }
        if (content.isMissingNode() || content.isNull()) {
            return "";
        }
        throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, "响应中 content 的格式无法识别");
    }

}
