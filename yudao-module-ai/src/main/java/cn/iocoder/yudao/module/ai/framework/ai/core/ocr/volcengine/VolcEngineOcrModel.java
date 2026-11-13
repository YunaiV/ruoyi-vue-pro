package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.volcengine;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.volcengine.model.Credentials;
import com.volcengine.model.ServiceInfo;
import com.volcengine.service.visual.IVisualService;
import com.volcengine.service.visual.impl.VisualServiceImpl;
import com.volcengine.service.visual.model.request.OCRNormalRequest;
import com.volcengine.service.visual.model.response.OCRNormalResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.getText;

/**
 * 火山引擎视觉智能通用文字识别 OCRNormal 的 {@link OcrModel} 实现（平台 BYTEDANCE）
 *
 * 使用官方视觉 SDK volc-sdk-java 的 {@link IVisualService}，
 * 请求 POST https://visual.volcengineapi.com?Action=OCRNormal&Version=2020-08-26，HMAC-SHA256 签名由 SDK 完成。
 * 项目已有的方舟 Ark runtime SDK 只提供大模型推理（对话、图片生成等），没有 OCRNormal 这个视觉接口，因此不用它。
 *
 * 凭据隔离：
 * 1. {@link VisualServiceImpl#getInstance()} 每次 new 一个实例，AK / SK 存在实例自己的 Credentials 中；
 * 2. 但实例默认引用 SDK 静态共享的 ServiceInfo（VisualConfig.serviceInfoMap），调用其 setHost / setScheme 会影响所有实例，
 *    因此本类总是为每个实例 new 一份独立的 ServiceInfo，并且不调用 setHost / setScheme。
 *
 * @author 芋道源码
 */
public class VolcEngineOcrModel implements OcrModel {

    public static final String PLATFORM = "BYTEDANCE";

    public static final String DEFAULT_BASE_URL = "https://visual.volcengineapi.com";
    public static final String DEFAULT_REGION = "cn-north-1";
    public static final String SERVICE = "cv";

    /**
     * 默认（也是目前唯一支持的）服务代码
     */
    public static final String DEFAULT_MODEL = "OCRNormal";

    /**
     * 成功的业务码
     */
    private static final int CODE_SUCCESS = 10000;

    /**
     * 支持 JPG、JPEG、PNG、BMP（PDF 首版不支持）；Base64 编码并 urlencode 后不超过 8MB
     */
    static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/bmp");
    static final long MAX_ENCODED_LENGTH = 8L * 1024 * 1024;

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private final String accessKeyId;
    private final String secretAccessKey;
    private final IVisualService visualService;

    public VolcEngineOcrModel(String apiKey, String url, String model) {
        this(apiKey, url, model, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    VolcEngineOcrModel(String apiKey, String url, String model, Duration connectTimeout, Duration readTimeout) {
        // 1. 解析凭据：AccessKeyId|SecretAccessKey，严格两段、均非空；异常消息不回显原值
        List<String> parts = StrUtil.split(apiKey, '|', true, false);
        if (parts.size() != 2 || StrUtil.hasBlank(parts.get(0), parts.get(1))) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CREDENTIAL,
                    "密钥格式应为 AccessKeyId|SecretAccessKey（两段，用 | 分隔，均不能为空）");
        }
        this.accessKeyId = parts.get(0);
        this.secretAccessKey = parts.get(1);
        // 2. 校验服务代码
        if (StrUtil.isNotBlank(model) && !DEFAULT_MODEL.equals(model.trim())) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG,
                    "不支持的服务代码：" + model.trim() + "，支持：" + DEFAULT_MODEL);
        }

        // 3. 每个实例：独立的 VisualServiceImpl + 独立的 ServiceInfo + 实例级 AK / SK
        IVisualService service = VisualServiceImpl.getInstance();
        service.setServiceInfo(buildServiceInfo(url, connectTimeout, readTimeout));
        service.setAccessKey(accessKeyId);
        service.setSecretKey(secretAccessKey);
        service.setRegion(DEFAULT_REGION);
        service.setConnectionTimeout((int) connectTimeout.toMillis());
        service.setSocketTimeout((int) readTimeout.toMillis());
        this.visualService = service;
    }

    /**
     * 构建本实例独占的 ServiceInfo：地址可覆盖，Credentials 只带 Region / Service（AK / SK 在实例的 Credentials 中）
     */
    private static ServiceInfo buildServiceInfo(String url, Duration connectTimeout, Duration readTimeout) {
        URI uri = URI.create(StrUtil.removeSuffix(StrUtil.blankToDefault(StrUtil.trim(url), DEFAULT_BASE_URL), "/"));
        if (StrUtil.isBlank(uri.getScheme()) || StrUtil.isBlank(uri.getRawAuthority())) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG, "地址格式不正确，应为 https://host");
        }
        // key 与 SDK VisualConfig 中的写法一致
        Map<String, Object> params = new HashMap<>();
        params.put("ConnectionTimeout", (int) connectTimeout.toMillis());
        params.put("SocketTimeout", (int) readTimeout.toMillis());
        params.put("Scheme", uri.getScheme());
        params.put("Host", uri.getRawAuthority());
        params.put("Header", new ArrayList<Header>(List.of(new BasicHeader("Accept", "application/json"))));
        params.put("Credentials", new Credentials(DEFAULT_REGION, SERVICE));
        return new ServiceInfo(params);
    }

    @Override
    public OcrResponse call(OcrPrompt prompt) {
        // 1. 校验图片，转 base64（仅内存中）
        byte[] image = OcrUtils.readImage(PLATFORM, prompt, ALLOWED_MIME_TYPES);
        String base64 = Base64.encode(image);
        OcrUtils.checkSize(PLATFORM, OcrUtils.urlEncodedLength(base64), MAX_ENCODED_LENGTH,
                "base64 编码并 urlencode 后的图片");

        // 2. 调用
        OCRNormalRequest request = new OCRNormalRequest();
        request.setImageBase64(base64);
        OCRNormalResponse response;
        try {
            response = visualService.ocrNormal(request);
        } catch (Exception ex) {
            throw convertException(ex);
        }

        // 3. 解析：code = 10000 为成功，data.line_texts 一行一条
        if (response == null) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, "响应为空");
        }
        if (response.getCode() != CODE_SUCCESS) {
            throw new OcrException(PLATFORM, typeOfBusinessCode(response.getCode()), String.valueOf(response.getCode()),
                    response.getRequestId(), OcrUtils.sanitize(response.getMessage(), accessKeyId, secretAccessKey));
        }
        if (response.getData() == null) {
            return new OcrResponse(new OcrResult(""));
        }
        return new OcrResponse(new OcrResult(OcrUtils.joinLines(response.getData().getLineTexts())));
    }

    /**
     * 转换 SDK 抛出的异常
     *
     * 1. HTTP 非 2xx：SDK 抛出 Exception(响应体)，响应体为 {"ResponseMetadata":{"RequestId","Error":{"Code","Message"}}}
     *    或视觉服务的 {"code","message","request_id"}
     * 2. 网络异常：SDK 抛出 Exception(EHTTP, cause)
     */
    private OcrException convertException(Exception ex) {
        JsonNode body = JsonUtils.parseObjectQuietly(ex.getMessage(), JsonNode.class);
        if (body != null && body.isObject()) {
            JsonNode metadata = body.get("ResponseMetadata");
            JsonNode error = metadata != null ? metadata.get("Error") : null;
            if (error != null && error.isObject()) {
                String code = getText(error, "Code");
                return new OcrException(PLATFORM, typeOfErrorCode(code), code, getText(metadata, "RequestId"),
                        OcrUtils.sanitize(getText(error, "Message"), accessKeyId, secretAccessKey));
            }
            if (body.has("code")) {
                int code = body.path("code").asInt();
                return new OcrException(PLATFORM, typeOfBusinessCode(code), String.valueOf(code),
                        getText(body, "request_id"),
                        OcrUtils.sanitize(getText(body, "message"), accessKeyId, secretAccessKey));
            }
        }
        if (ex.getCause() != null || ex instanceof IOException) {
            return OcrUtils.convertNetworkException(PLATFORM, ex);
        }
        return new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR,
                "调用失败：" + OcrUtils.sanitize(ex.getMessage(), accessKeyId, secretAccessKey));
    }

    /**
     * 公共错误码（ResponseMetadata.Error.Code）
     */
    static OcrException.Type typeOfErrorCode(String code) {
        if (code == null) {
            return OcrException.Type.UPSTREAM_ERROR;
        }
        if (code.contains("Signature") || code.contains("AccessKey") || code.contains("Credential")
                || code.contains("AccessDenied") || code.contains("Authorization") || code.contains("Unauthorized")) {
            return OcrException.Type.AUTH_FAILED;
        }
        if (code.contains("FlowLimit") || code.contains("Throttl") || code.contains("RequestLimit")
                || code.contains("TooManyRequests")) {
            return OcrException.Type.RATE_LIMITED;
        }
        return OcrException.Type.UPSTREAM_ERROR;
    }

    /**
     * 视觉服务业务码（code）：50429 QPS 超限、50430 并发超限；其它按上游错误处理
     */
    static OcrException.Type typeOfBusinessCode(int code) {
        if (code == 50429 || code == 50430) {
            return OcrException.Type.RATE_LIMITED;
        }
        return OcrException.Type.UPSTREAM_ERROR;
    }

}
