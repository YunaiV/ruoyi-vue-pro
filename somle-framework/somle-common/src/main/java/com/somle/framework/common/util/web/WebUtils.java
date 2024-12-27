package com.somle.framework.common.util.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somle.framework.common.util.io.SomleResponse;
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
import java.net.SocketTimeoutException;
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


    public static void sendRequest(RequestX requestX, BiConsumer<Response, Exception> callback) {
        try (Response response = client.newCall(toOkHttp(requestX)).execute()) {
            callback.accept(response, null);
        } catch (IOException e) {
            callback.accept(null, e);
        }
    }

    /**
     * 发送HTTP请求并根据指定的响应类型处理返回的数据。
     * 此方法会尝试执行一个HTTP请求，然后根据指定的ResponseType来解析响应数据。
     * 如果HTTP响应不成功，它将通过IOException报告错误。
     *
     * @param <T>          泛型类型参数，responseType入参指定的数据类型。
     * @param requestX     包含请求所需数据的请求对象。
     * @param responseType 指定返回数据应被解析的格式，可以是STRING, BYTES, BYTE_STRING等。
     * @return 返回一个SomleResponse对象，其中包含从响应中解析得到的数据和响应码。
     * @throws IOException              如果请求不成功或响应数据无法正确解析，将抛出此异常。
     * @throws IllegalArgumentException 如果解析JSON数据时发生错误，将抛出此异常。
     * @see SomleResponse
     */
    public static <T> SomleResponse<T> sendRequest(RequestX requestX, SomleResponse.ResponseType responseType) throws IOException, IllegalArgumentException {
        try (Response response = client.newCall(toOkHttp(requestX)).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response.code() + " with message: " + response.message());
            }

            try {
                T responseBodyData = parseResponse(response, responseType);
                int responseCode = response.code();
                return new SomleResponse<>(responseCode, responseBodyData);
            } catch (JsonParseException e) {
                // 处理JSON解析异常
                throw new IllegalArgumentException("JSON parsing error: " + e.getMessage(), e);
            }
        } catch (Exception e) { // 捕获更广泛的异常
            // 可以根据异常类型提供更具体的异常处理
            if (e instanceof SocketTimeoutException) {
                throw new IOException("Request timed out", e);
            } else if (e instanceof RuntimeException) {
                throw new RuntimeException("Runtime exception occurred", e);
            } else {
                throw new IOException("An unexpected error occurred", e);
            }
        }
    }

    /**
     * 根据响应类型解析响应体
     *
     * @param response     响应对象
     * @param responseType 需要解析的响应类型
     * @return 解析后的数据，类型为泛型T
     * @throws IOException 如果读取响应体失败
     */
    @SuppressWarnings("unchecked")
    private static <T> T parseResponse(Response response, SomleResponse.ResponseType responseType) throws IOException {
        ResponseBody body = response.body();
        if (body == null) throw new IOException("Response body is null");

        return switch (responseType) {
            case STRING -> (T) body.string();
            case BYTES -> (T) body.bytes();
            case BYTE_STRING -> (T) body.byteString();
            // Add a case for SOURCE if needed, handle it properly.
            default ->
                throw new IllegalArgumentException("Unsupported response type: " + responseType.getDescription());
        };
    }


    @SneakyThrows
    public static String getBodyString(Response response) {
        if (response.body() == null) {
            throw new RuntimeException("body is null");
        }
        String responseString = response.body()
            .string();
        log.debug("response: " + responseString);
        return responseString;
    }

    public static <T> T parseResponse(Response response, Class<T> responseClass) {
        return JsonUtils.parseObject(getBodyString(response), responseClass);
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
        if ("gzip".equalsIgnoreCase(compression)) {
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
    public static <T> T parseResponse(String response, TypeReference<T> valueTypeRef) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response", e);
        }
    }
}
