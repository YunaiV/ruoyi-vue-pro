package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.deepseek;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrUtils;
import tools.jackson.databind.JsonNode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.getText;

/**
 * DeepSeek 官方 OCR 的 {@link OcrModel} 实现（平台 DEEPSEEK）
 *
 * 1. multipart/form-data 只发送 file、purpose 两个字段：多传其它字段会返回 400
 * 2. 模型标识即 purpose：text_recognition（默认）、rich_layout_recognition
 * 3. 识别过程中服务端持续发送换行符保持连接，换行符属于 JSON 的前导空白，整个响应体直接按 JSON 解析
 * 4. 识别开始后状态码已是 200，失败时响应体是 {"error":{...}}，需要检查
 *
 * 官方文档：https://api-docs.deepseek.com/zh-cn/guides/ocr
 *
 * @author 芋道源码
 */
public class DeepSeekOcrModel implements OcrModel {

    public static final String PLATFORM = "DEEPSEEK";

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String OCR_PATH = "/v1/tools/ocr";

    public static final String PURPOSE_TEXT_RECOGNITION = "text_recognition";
    public static final String PURPOSE_RICH_LAYOUT_RECOGNITION = "rich_layout_recognition";
    static final Set<String> PURPOSES = Set.of(PURPOSE_TEXT_RECOGNITION, PURPOSE_RICH_LAYOUT_RECOGNITION);

    /**
     * 官方支持的图片类型（文件格式由实际内容判断，这里只做图片识别）
     */
    static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif",
            "image/bmp", "image/tiff");

    /**
     * 逐页识别的特殊标记：<｜ref｜>标题<｜/ref｜><｜box｜>[[x1, y1, x2, y2]]<｜/box｜>
     *
     * 保留 ref 中的文字，去掉 ref 标记本身和整个 box 坐标块
     */
    private static final Pattern BOX_PATTERN = Pattern.compile("<｜box｜>.*?<｜/box｜>", Pattern.DOTALL);
    private static final Pattern REF_TAG_PATTERN = Pattern.compile("<｜/?ref｜>");

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    /**
     * 读超时：官方总时长 180 秒，这里需小于前端的 120 秒请求超时
     */
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(100);

    private final String apiKey;
    private final String purpose;
    private final RestClient restClient;

    public DeepSeekOcrModel(String apiKey, String url, String model) {
        this(apiKey, url, model, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    DeepSeekOcrModel(String apiKey, String url, String model, Duration connectTimeout, Duration readTimeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CREDENTIAL, "API Key 不能为空");
        }
        this.apiKey = apiKey.trim();
        this.purpose = StrUtil.blankToDefault(StrUtil.trim(model), PURPOSE_TEXT_RECOGNITION);
        if (!PURPOSES.contains(this.purpose)) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG,
                    "模型 " + this.purpose + " 不支持，可选：" + String.join("、", PURPOSES));
        }
        this.restClient = OcrUtils.buildRestClient(StrUtil.blankToDefault(url, DEFAULT_BASE_URL),
                connectTimeout, readTimeout);
    }

    @Override
    public OcrResponse call(OcrPrompt prompt) {
        // 1. 校验图片
        byte[] image = OcrUtils.readImage(PLATFORM, prompt, ALLOWED_MIME_TYPES);
        String mimeType = OcrUtils.normalizeMimeType(prompt.getInstructions().getMimeType());
        final String filename = "image." + StrUtil.subAfter(mimeType, "/", false);

        // 2. 调用：file 必须带文件名
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new ByteArrayResource(image) {

            @Override
            public String getFilename() {
                return filename;
            }

        });
        form.add("purpose", purpose);
        String body;
        try {
            body = restClient.post().uri(OCR_PATH)
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw OcrUtils.convertRestClientException(PLATFORM, ex, apiKey);
        }

        // 3. 解析
        return new OcrResponse(new OcrResult(parseText(body, apiKey)));
    }

    private static String parseText(String body, String apiKey) {
        // 不用 JsonUtils.parseTree：解析失败时它会把原文（识别结果）打进日志
        JsonNode root = JsonUtils.parseObjectQuietly(body, JsonNode.class);
        if (root == null) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, "响应不是合法的 JSON");
        }
        // 识别开始后的失败：HTTP 200 + {"error":{"message","type","code"}}
        JsonNode error = root.get("error");
        if (error != null && error.isObject()) {
            OcrException.Type type = "gateway_timeout_error".equals(getText(error, "type"))
                    ? OcrException.Type.TIMEOUT : OcrException.Type.UPSTREAM_ERROR;
            throw new OcrException(PLATFORM, type, getText(error, "code"), getText(root, "id"),
                    OcrUtils.sanitize(getText(error, "message"), apiKey));
        }
        JsonNode pages = root.get("pages");
        if (pages == null || !pages.isArray()) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, null,
                    getText(root, "id"), "响应中没有 pages");
        }
        // 按页码排序后拼接
        List<JsonNode> pageList = new ArrayList<>();
        pages.forEach(pageList::add);
        pageList.sort(Comparator.comparingInt(page -> page.path("index").asInt()));
        List<String> texts = new ArrayList<>();
        for (JsonNode page : pageList) {
            texts.add(cleanText(page.path("text").asText("")));
        }
        return OcrUtils.joinLines(texts);
    }

    /**
     * 去掉逐页识别的特殊标记，保留 ref 中的文字
     */
    static String cleanText(String text) {
        String result = BOX_PATTERN.matcher(text).replaceAll("");
        return REF_TAG_PATTERN.matcher(result).replaceAll("");
    }

}
