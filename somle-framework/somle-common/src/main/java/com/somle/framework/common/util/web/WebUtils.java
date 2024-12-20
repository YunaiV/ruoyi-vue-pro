package com.somle.framework.common.util.web;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import cn.hutool.json.JSONUtil;
import com.somle.framework.common.util.csv.CsvUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import lombok.SneakyThrows;
import okhttp3.*;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebUtils {
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();



    public static String urlWithParams(String url, MultiValuedMap<String,String> queryParams) {
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


    public static Headers toHeaders(MultiValuedMap<String,String> headerMap) {
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
        return client.newCall(toOkHttp(requestX)).execute();
    }

    @SneakyThrows
    public static <T> T sendRequest(RequestX requestX, Class<T> responseClass) {
        var response = client.newCall(toOkHttp(requestX)).execute();
        return parseResponse(response, responseClass);
    }





    @SneakyThrows
    public static String getBodyString(Response response) {
        assert response.body() != null;
        String responseString = response.body().string();
        log.debug("response: " + responseString);
        return responseString;
    }

    public static <T> T parseResponse(Response response, Class<T> responseClass) {
        return JsonUtils.parseObject(getBodyString(response), responseClass);
    }


    @SneakyThrows
    public static String urlToString(String urlString, String compression) {
        InputStream inputStream = getInputStreamFromUrl(urlString, compression);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toString();
    }

    @SneakyThrows
    public static List<Map<String,String>> csvToJson(String urlString) {
        InputStream inputStream = getInputStreamFromUrl(urlString, null);
        List<Map<String,String>> maps = CsvUtils.readTsvFromInputStream(inputStream);
        return maps;
    }

    @SneakyThrows
    private static InputStream getInputStreamFromUrl(String urlString, String compression) {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();
        if ("gzip".equalsIgnoreCase(compression)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }
//    public static <T> T parallelRun(int parallelism, Callable<T> codeBlock) {
//        ForkJoinPool customThreadPool = new ForkJoinPool(parallelism);
//        var result = customThreadPool.submit(codeBlock).join();
//        customThreadPool.shutdown();
//        return result;
//    }

    // TODO: a general method to handle http exception (429, timeout etc)


}
