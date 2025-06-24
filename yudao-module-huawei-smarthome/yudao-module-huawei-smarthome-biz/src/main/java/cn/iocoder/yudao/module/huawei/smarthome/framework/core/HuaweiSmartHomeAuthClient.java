package cn.iocoder.yudao.module.huawei.smarthome.framework.core;

import cn.iocoder.yudao.module.huawei.smarthome.config.HuaweiSmartHomeProperties;
import cn.iocoder.yudao.module.huawei.smarthome.util.HuaweiSmartHomeAuthUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HuaweiSmartHomeAuthClient {

    private final HuaweiSmartHomeProperties properties;
    private final OkHttpClient okHttpClient;

    public HuaweiSmartHomeAuthClient(HuaweiSmartHomeProperties properties) {
        this.properties = properties;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                // 可以添加其他配置，如连接池、重试拦截器等
                .build();
    }

    public static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8"); // Corrected charset

    public String sendRequest(String resourcePath, String httpMethod, String requestBodyJson) throws IOException {
        if (properties.getEndpoint() == null || properties.getEndpoint().isEmpty()) {
            log.error("[sendRequest] Huawei SmartHome API Endpoint is not configured.");
            throw new IOException("Huawei SmartHome API Endpoint is not configured.");
        }
        String url = properties.getEndpoint() + resourcePath;
        String requestId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String bodyForSign = (requestBodyJson == null || "GET".equalsIgnoreCase(httpMethod) || "DELETE".equalsIgnoreCase(httpMethod)) ? "" : requestBodyJson;

        // 重要：确保 properties.getSecretKey() 存储的是16进制字符串形式的SK
        String xSign = HuaweiSmartHomeAuthUtils.calculateSignWithHexSecret(
                properties.getAccessKey(),
                properties.getSecretKey(), // 华为文档要求SK是16进制字符串
                properties.getProjectId(),
                requestId,
                timestamp,
                bodyForSign
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Accept", "application/json")
                .addHeader("x-access-key", properties.getAccessKey())
                .addHeader("x-project-id", properties.getProjectId())
                .addHeader("x-request-id", requestId)
                .addHeader("x-timestamp", timestamp)
                .addHeader("x-sign", xSign);

        RequestBody body = null;
        if (requestBodyJson != null && ("POST".equalsIgnoreCase(httpMethod) || "PUT".equalsIgnoreCase(httpMethod))) {
            body = RequestBody.create(requestBodyJson, JSON_MEDIA_TYPE);
        }

        switch (httpMethod.toUpperCase()) {
            case "POST":
                requestBuilder.post(Objects.requireNonNull(body, "POST request body cannot be null"));
                break;
            case "PUT":
                requestBuilder.put(Objects.requireNonNull(body, "PUT request body cannot be null"));
                break;
            case "DELETE":
                requestBuilder.delete(); // DELETE请求通常没有body，即使有，OkHttp也支持 delete(RequestBody)
                break;
            case "GET":
            default: // 默认为GET
                requestBuilder.get();
                break;
        }

        Request request = requestBuilder.build();
        log.info("[sendRequest][华为API请求] URL: {}, Method: {}, Headers: {}, Body: {}", url, httpMethod, request.headers(), requestBodyJson);

        try (Response response = okHttpClient.newCall(request).execute()) {
            String responseBodyString = response.body() != null ? response.body().string() : null;
            log.info("[sendRequest][华为API响应] URL: {}, Status: {}, Response: {}", url, response.code(), responseBodyString);

            if (!response.isSuccessful()) {
                // 可以根据华为的错误码结构进一步解析错误信息
                String errorMsg = String.format("华为API请求失败: URL=%s, HTTP Status=%d, Response=%s",
                        url, response.code(), responseBodyString);
                log.error(errorMsg);
                // TODO: 定义统一的异常体系，并抛出相应的业务异常
                throw new IOException(errorMsg);
            }
            return responseBodyString;
        } catch (IOException e) {
            log.error("[sendRequest][华为API请求异常] URL: {}, Method: {}, Error: {}", url, httpMethod, e.getMessage(), e);
            throw e;
        }
    }

    // 便捷方法
    public String get(String resourcePath) throws IOException {
        return sendRequest(resourcePath, "GET", null);
    }

    public String post(String resourcePath, @NotNull String requestBodyJson) throws IOException {
        return sendRequest(resourcePath, "POST", requestBodyJson);
    }

    public String put(String resourcePath, @NotNull String requestBodyJson) throws IOException {
        return sendRequest(resourcePath, "PUT", requestBodyJson);
    }

    public String delete(String resourcePath) throws IOException {
        return sendRequest(resourcePath, "DELETE", null);
    }
     public String delete(String resourcePath, String requestBodyJson) throws IOException {
        return sendRequest(resourcePath, "DELETE", requestBodyJson);
    }
}
