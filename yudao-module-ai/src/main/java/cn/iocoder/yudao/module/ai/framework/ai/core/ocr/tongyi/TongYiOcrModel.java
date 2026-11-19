package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tongyi;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrUtils;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.common.Status;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.protocol.ConnectionOptions;
import com.alibaba.dashscope.protocol.Protocol;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 阿里百炼 Qwen-OCR 的 {@link OcrModel} 实现（平台 ALIBABA）
 *
 * 使用项目已引入的 DashScope SDK（dashscope-sdk-java）的 {@link MultiModalConversation}，与官方 Qwen-OCR 的 Java 示例一致：
 * 图片以 data URL（base64）放在消息的 image 中。每个实例持有自己的地址和超时，API Key 随请求传入，不读全局配置。
 * 官方文档：https://help.aliyun.com/zh/model-studio/qwen-vl-ocr
 *
 * @author 芋道源码
 */
public class TongYiOcrModel implements OcrModel {

    public static final String PLATFORM = "ALIBABA";

    /**
     * 默认地址：与 DashScope 的 baseUrl 写法一致，SDK 的 /api/v1 拼在后面
     */
    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";
    private static final String API_PATH = "/api/v1";

    /**
     * 默认模型：qwen-vl-ocr（稳定版）；也可配置 qwen3.5-ocr、qwen-vl-ocr-2025-11-20 等
     */
    public static final String DEFAULT_MODEL = "qwen-vl-ocr";

    /**
     * 官方文档中的默认 Prompt：只输出图片中的文字，不加描述和格式
     */
    static final String PROMPT = "Please output only the text content from the image without any additional descriptions or formatting.";

    /**
     * 支持的图片类型（4K 以下分辨率）：BMP、JPEG、PNG、TIFF、WEBP、HEIC
     */
    static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/bmp", "image/jpeg", "image/png",
            "image/tiff", "image/webp", "image/heic");
    /**
     * Base64 编码后字符串不超过 10MB（这里按完整 data URL 计算，更保守）
     */
    static final long MAX_DATA_URL_LENGTH = 10L * 1024 * 1024;

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

    private final String apiKey;
    private final String model;
    private final MultiModalConversation conversation;

    public TongYiOcrModel(String apiKey, String url, String model) {
        this(apiKey, url, model, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    TongYiOcrModel(String apiKey, String url, String model, Duration connectTimeout, Duration readTimeout) {
        // 1. 原始 API Key，不拆分
        if (StrUtil.isBlank(apiKey)) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CREDENTIAL, "API Key 不能为空");
        }
        this.apiKey = apiKey.trim();
        // 2. 只接受 OCR 专用模型（名称包含 ocr），避免误配成普通对话模型
        this.model = StrUtil.blankToDefault(StrUtil.trim(model), DEFAULT_MODEL);
        if (!this.model.toLowerCase(Locale.ROOT).contains("ocr")) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG, "模型 " + this.model + " 不是 Qwen-OCR 模型");
        }
        // 3. 实例级的地址和超时
        String baseUrl = StrUtil.removeSuffix(StrUtil.blankToDefault(StrUtil.trim(url), DEFAULT_BASE_URL), "/") + API_PATH;
        this.conversation = new MultiModalConversation(Protocol.HTTP.getValue(), baseUrl, ConnectionOptions.builder()
                .connectTimeout(connectTimeout).readTimeout(readTimeout).writeTimeout(readTimeout).build());
    }

    @Override
    public OcrResponse call(OcrPrompt prompt) {
        // 1. 校验图片，转 data URL（仅内存中）
        byte[] image = OcrUtils.readImage(PLATFORM, prompt, ALLOWED_MIME_TYPES);
        String mimeType = OcrUtils.normalizeMimeType(prompt.getInstructions().getMimeType());
        String dataUrl = "data:" + mimeType + ";base64," + Base64.encode(image);
        OcrUtils.checkSize(PLATFORM, dataUrl.length(), MAX_DATA_URL_LENGTH, "base64 编码后的图片");

        // 2. 调用
        MultiModalMessage message = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(List.of(Map.of("image", dataUrl), Map.of("text", PROMPT))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey).model(model).message(message).build();
        MultiModalConversationResult result;
        try {
            result = conversation.call(param);
        } catch (ApiException ex) {
            throw convertException(ex);
        } catch (NoApiKeyException | UploadFileException ex) {
            // API Key 已在构造时校验、data URL 不会触发上传，正常不会走到这里
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, "调用失败（" + ex.getClass().getSimpleName() + "）");
        }

        // 3. 解析：output.choices[0].message.content[].text
        List<MultiModalConversationOutput.Choice> choices = result.getOutput() != null ? result.getOutput().getChoices() : null;
        if (CollUtil.isEmpty(choices) || choices.get(0).getMessage() == null) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, null, result.getRequestId(), "响应中没有 choices");
        }
        String text = CollUtil.emptyIfNull(choices.get(0).getMessage().getContent()).stream()
                .map(part -> part.get("text")).filter(Objects::nonNull).map(Object::toString)
                .collect(Collectors.joining());
        return new OcrResponse(new OcrResult(OcrUtils.normalizeText(text)));
    }

    /**
     * 转换 SDK 异常：HTTP 错误保留状态码、错误码、错误信息和请求 ID；网络异常 SDK 记为 statusCode = -1
     */
    private OcrException convertException(ApiException ex) {
        Status status = ex.getStatus();
        if (status == null || status.getStatusCode() <= 0) {
            return OcrUtils.convertNetworkException(PLATFORM, ex);
        }
        // 非 JSON 的错误响应（如网关的 HTML），SDK 会把响应体原样放进 message，因此不带上
        String message = "HTTP " + status.getStatusCode() + (status.isJson() && StrUtil.isNotBlank(status.getMessage())
                ? "：" + OcrUtils.sanitize(status.getMessage(), apiKey) : "");
        return new OcrException(PLATFORM, OcrUtils.typeOfHttpStatus(status.getStatusCode()),
                StrUtil.emptyToNull(status.getCode()), StrUtil.emptyToNull(status.getRequestId()), message);
    }

}
