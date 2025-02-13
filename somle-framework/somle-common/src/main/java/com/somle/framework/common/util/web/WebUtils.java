package com.somle.framework.common.util.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;

@Slf4j
public class WebUtils {
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();


    public static String urlWithParams(String url, MultiValuedMap<String, String> queryParams) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> queryParam : queryParams.entries()) {
            urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
        }
        return urlBuilder.build().url().toString();
    }

    public static String urlWithParams(String url, JSONObject json) {
        MultiValuedMap<String, String> queryParams = JsonUtils.toMultiStringMap(json);
        return urlWithParams(url, queryParams);
    }

    // TODO: Depreciated
    /**
     * use pojo's json format to make queryparams
     */
    public static String urlWithParams(String url, Object pojo) {
        var json = JsonUtils.toJSONObject(pojo);
        return urlWithParams(url, json);
    }


    public static Headers toHeaders(MultiValuedMap<String, String> headerMap) {
        var headersBuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : headerMap.entries()) {
            headersBuilder.add(entry.getKey(), entry.getValue());
        }
        return headersBuilder.build();
    }

    public static Headers toHeaders(JSONObject json) {
        MultiValuedMap<String, String> headerMap = JsonUtils.toMultiStringMap(json);
        return toHeaders(headerMap);
    }

    // TODO: Depreciated
    /*
     * use pojo's json format to make headers
     */
    public static Headers toHeaders(Object pojo) {
        var json = JsonUtils.toJSONObject(pojo);
        return toHeaders(json);
    }


    public static Headers merge(Headers headers1, Headers headers2) {
        Headers.Builder builder = new Headers.Builder();

        // Add headers from the first Headers object
        for (String name : headers1.names()) {
            builder.add(name, headers1.get(name));
        }

        // Add headers from the second Headers object, potentially overriding duplicates
        for (String name : headers2.names()) {
//            builder.removeAll(name); // Remove duplicates to keep the second headers' values
            builder.add(name, headers2.get(name));
        }

        return builder.build();
    }

    public static Request toOkHttp(RequestX requestX) {
        var requestMethod = requestX.getRequestMethod();
        var url = requestX.getUrl();
        var queryParams = requestX.getQueryParams();
        var headers = requestX.getHeaders();
        var payload = requestX.getPayload();

        String fullUrl = url;
        if (queryParams != null) {
            if (queryParams instanceof Map) {
                fullUrl = urlWithParams(url, (Map) queryParams);
            } else {
                fullUrl = urlWithParams(url, queryParams);
            }
        }

        log.debug("full url: " + fullUrl);
        Request.Builder requestBuilder = new Request.Builder()
            .url(fullUrl);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
            log.debug("headers: " + headers.toString());
        }

        String bodyString = JsonUtils.toJsonString(payload);
        RequestBody body = RequestBody.create(bodyString, MediaType.parse("application/json; charset=utf-8"));
        log.debug("body: " + bodyString);


        Request request;
        switch (requestMethod) {
            case POST:
                request = requestBuilder.post(body).build();
                break;

            default:
                request = requestBuilder.build();
                break;
        }

        return request;

    }

    @SneakyThrows
    public static Response sendRequest(RequestX requestX) {
        return client.newCall(toOkHttp(requestX))
            .execute();
    }

    public static <T> T sendRequest(RequestX requestX, Class<T> responseClass) {
        // 使用try-with-resources确保response被关闭
        try (Response response = client.newCall(toOkHttp(requestX))
            .execute()) {
            return parseResponse(response, responseClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送一个 HTTP 请求并处理响应或异常。
     *
     * @param requestX 请求对象，包含了需要发送的请求的详细信息。
     * @param callback 回调函数，用于处理响应结果或异常。
     */
    public static void sendRequest(RequestX requestX, BiConsumer<Response, Exception> callback) {
        try (Response response = client.newCall(toOkHttp(requestX)).execute()) {
            callback.accept(response, null);
        } catch (IOException e) {
            callback.accept(null, e);
        }
    }


    /**
     * 获取 HTTP 响应的 Body 内容并返回为字符串
     *
     * @param response OkHttp 的 Response 对象
     * @return 响应 Body 的字符串
     * @throws IllegalStateException 如果 response 或 response body 为空
     * @throws RuntimeException 如果读取 response body 发生 IOException
     */
    public static String getBodyString(Response response) {
        if (response == null || response.body() == null) {
            throw new IllegalStateException("Response or body is null");
        }
        try (ResponseBody responseBody = response.body()) {
            String responseString = responseBody.string();
            log.debug("Response: {}", responseString);
            return responseString;
        } catch (IOException e) {
            log.error("Failed to read response body", e);
            throw new RuntimeException("Error reading response body", e);
        }
    }


    public static <T> T parseResponse(Response response, Class<T> responseClass) {
        String bodyString = "";
        try {
            bodyString = getBodyString(response);
            return JsonUtils.parseObject(bodyString, responseClass);
        } catch (Exception e) {
            throw new RuntimeException("parse error in response with body: \n" + bodyString + "\ncause: " + e);
        }

    }


    @SneakyThrows
    public static String urlToString(String urlString, String compression) {
        InputStream inputStream = urlToInputStream(urlString, compression);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toString();
    }


    @SneakyThrows
    private static InputStream urlToInputStream(String urlString, String compression) {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();
        if ("GZIP".equalsIgnoreCase(compression)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }


    // TODO: a general method to handle http exception (429, timeout etc)


    /**
     * 解析响应字符串为指定类型的对象
     *
     * @param response     响应的 JSON 字符串
     * @param valueTypeRef 用于反序列化的目标类型的 TypeReference
     * @param <T>          返回对象的类型
     * @return 反序列化后的对象
     */
    public static <T> T parseResponse(Response response, TypeReference<T> valueTypeRef) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(getBodyString(response), valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response", e);
        }
    }
}
