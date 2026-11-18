package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tencent;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrUtils;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 腾讯云通用文字识别（高精度版）GeneralAccurateOCR 的 {@link OcrModel} 实现（平台 TENCENT）
 *
 * 使用官方 SDK tencentcloud-sdk-java-ocr；每个实例持有自己的 Credential 和 OcrClient，不同密钥互不影响。
 * 官方文档：https://cloud.tencent.com/document/api/866/34937
 *
 * @author 芋道源码
 */
public class TencentOcrModel implements OcrModel {

    public static final String PLATFORM = "TENCENT";

    /**
     * 默认接入点和地域（GeneralAccurateOCR 的 Region 为可选参数）
     */
    public static final String DEFAULT_ENDPOINT = "ocr.tencentcloudapi.com";
    public static final String DEFAULT_REGION = "ap-guangzhou";

    /**
     * 默认（也是目前唯一支持的）服务代码
     */
    public static final String DEFAULT_MODEL = "GeneralAccurateOCR";

    /**
     * 支持 PNG、JPG、JPEG、BMP（PDF 首版不支持）；Base64 编码后不超过 10M
     */
    static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/bmp");
    static final long MAX_BASE64_LENGTH = 10L * 1024 * 1024;

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 错误码：图片中没有文字。此时按“识别成功、结果为空”处理
     */
    private static final String ERROR_CODE_IMAGE_NO_TEXT = "FailedOperation.ImageNoText";

    private final String secretId;
    private final String secretKey;
    private final OcrClient client;

    public TencentOcrModel(String apiKey, String url, String model) {
        this(apiKey, url, model, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    TencentOcrModel(String apiKey, String url, String model, Duration connectTimeout, Duration readTimeout) {
        // 1. 解析凭据：SecretId|SecretKey，严格两段、均非空；异常消息不回显原值
        List<String> parts = StrUtil.split(apiKey, '|', true, false);
        if (parts.size() != 2 || StrUtil.hasBlank(parts.get(0), parts.get(1))) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CREDENTIAL,
                    "密钥格式应为 SecretId|SecretKey（两段，用 | 分隔，均不能为空）");
        }
        this.secretId = parts.get(0);
        this.secretKey = parts.get(1);
        // 2. 校验服务代码
        if (StrUtil.isNotBlank(model) && !DEFAULT_MODEL.equals(model.trim())) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG,
                    "不支持的服务代码：" + model.trim() + "，支持：" + DEFAULT_MODEL);
        }

        // 3. 每个实例独立的 HttpProfile / ClientProfile / OcrClient；SDK 的超时单位为秒
        HttpProfile httpProfile = new HttpProfile();
        applyUrl(httpProfile, url);
        int readTimeoutSeconds = (int) Math.max(1, readTimeout.toSeconds());
        httpProfile.setConnTimeout((int) Math.max(1, connectTimeout.toSeconds()));
        httpProfile.setReadTimeout(readTimeoutSeconds);
        httpProfile.setWriteTimeout(readTimeoutSeconds);
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        this.client = new OcrClient(new Credential(secretId, secretKey), DEFAULT_REGION, clientProfile);
    }

    /**
     * 地址覆盖：支持 https://ocr.tencentcloudapi.com 或 ocr.tencentcloudapi.com 两种写法；为空时使用默认接入点
     */
    private static void applyUrl(HttpProfile httpProfile, String url) {
        String value = StrUtil.removeSuffix(StrUtil.blankToDefault(StrUtil.trim(url), DEFAULT_ENDPOINT), "/");
        if (!value.contains("://")) {
            httpProfile.setEndpoint(value);
            return;
        }
        URI uri = URI.create(value);
        if (StrUtil.isBlank(uri.getScheme()) || StrUtil.isBlank(uri.getRawAuthority())) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG, "地址格式不正确");
        }
        httpProfile.setProtocol(uri.getScheme() + "://");
        httpProfile.setEndpoint(uri.getRawAuthority());
    }

    @Override
    public OcrResponse call(OcrPrompt prompt) {
        // 1. 校验图片，转 base64（仅内存中）
        byte[] image = OcrUtils.readImage(PLATFORM, prompt, ALLOWED_MIME_TYPES);
        String base64 = Base64.encode(image);
        OcrUtils.checkSize(PLATFORM, base64.length(), MAX_BASE64_LENGTH, "base64 编码后的图片");

        // 2. 调用
        GeneralAccurateOCRRequest request = new GeneralAccurateOCRRequest();
        request.setImageBase64(base64);
        GeneralAccurateOCRResponse response;
        try {
            response = client.GeneralAccurateOCR(request);
        } catch (TencentCloudSDKException ex) {
            if (ERROR_CODE_IMAGE_NO_TEXT.equals(ex.getErrorCode())) {
                return new OcrResponse(new OcrResult(""));
            }
            throw convertException(ex);
        }

        // 3. 解析：TextDetections[].DetectedText，一行一条
        TextDetection[] detections = response.getTextDetections();
        if (detections == null) {
            return new OcrResponse(new OcrResult(""));
        }
        return new OcrResponse(new OcrResult(OcrUtils.joinLines(
                Arrays.stream(detections).map(TextDetection::getDetectedText).toList())));
    }

    private OcrException convertException(TencentCloudSDKException ex) {
        String code = StrUtil.emptyToNull(ex.getErrorCode());
        // 无错误码：SDK 侧的网络异常，不透传原始消息
        if (code == null && ex.getCause() != null) {
            return OcrUtils.convertNetworkException(PLATFORM, ex);
        }
        return new OcrException(PLATFORM, typeOfErrorCode(code), code, StrUtil.emptyToNull(ex.getRequestId()),
                OcrUtils.sanitize(ex.getMessage(), secretId, secretKey));
    }

    /**
     * 按腾讯云错误码推断错误类型，参见 https://cloud.tencent.com/document/api/866/34937 的“错误码”
     */
    static OcrException.Type typeOfErrorCode(String code) {
        if (code == null) {
            return OcrException.Type.UPSTREAM_ERROR;
        }
        if (code.startsWith("AuthFailure") || code.startsWith("UnauthorizedOperation")
                || code.startsWith("ResourceUnavailable") || code.startsWith("ResourcesSoldOut")) {
            return OcrException.Type.AUTH_FAILED;
        }
        if (code.contains("Image") || code.contains("TooLargeFile") || code.startsWith("InvalidParameter")) {
            return OcrException.Type.INVALID_IMAGE;
        }
        if (code.startsWith("RequestLimitExceeded") || code.startsWith("LimitExceeded")) {
            return OcrException.Type.RATE_LIMITED;
        }
        if (code.contains("Timeout")) {
            return OcrException.Type.TIMEOUT;
        }
        return OcrException.Type.UPSTREAM_ERROR;
    }

}
