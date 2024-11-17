package com.somle.framework.common.util.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.object.BeanUtils;
import lombok.SneakyThrows;
import okhttp3.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONArray;
//import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebUtils {
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();



    public static String urlWithParams(String url, Map<String,String> queryParams) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
            urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
        }
        return urlBuilder.build().toString();
    }

    public static String urlWithParams(String url, JSONObject json) {
        Map<String, String> queryParams = JsonUtils.toStringMap(json);
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


    public static Headers toHeaders(Map<String,String> headerMap) {
        var headersBuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            headersBuilder.add(entry.getKey(), entry.getValue());
        }
        return headersBuilder.build();
    }

    public static Headers toHeaders(JSONObject json) {
        Map<String, String> headerMap = JsonUtils.toStringMap(json);
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

    @SneakyThrows
    public static Response sendRequest(String requestMethod, String url, Object queryParams, Map<String, String> headers, Object payload) {
        log.debug("method: " + requestMethod);
        log.debug("url: " + url);
        String fullUrl = null;
        if (queryParams instanceof Map) {
            fullUrl = urlWithParams(url, (Map) queryParams);
        } else {
            fullUrl = urlWithParams(url, queryParams);
        }
        log.debug("full url: " + fullUrl);

        String bodyString = JsonUtils.toJsonString(payload);
        RequestBody body = RequestBody.create(bodyString, MediaType.parse("application/json; charset=utf-8"));
        log.debug("body: " + bodyString);
        
        Request.Builder requestBuilder = new Request.Builder()
            .url(fullUrl);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        log.debug("headers: " + headers.toString());

        Request request = null;
        switch (requestMethod) {
            case "POST":
                request = requestBuilder.post(body).build();
                break;
        
            default:
                request = requestBuilder.build();
                break;
        }

        return client.newCall(request).execute();

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


    public static <T> T getRequest(String url, Object queryParams, Map<String, String> headers, Class<T> responseClass) {
        var response = sendRequest("GET", url, queryParams, headers, null);
        return parseResponse(response, responseClass);
    }

    public static <T> T postRequest(String url, Object queryParams, Map<String, String> headers, Object payload, Class<T> responseClass) {
        var response =  sendRequest("POST", url, queryParams, headers, payload);
        return parseResponse(response, responseClass);
    }

    public static Response getRequest(String url, Object queryParams, Map<String, String> headers) {
        return sendRequest("GET", url, queryParams, headers, null);
    }

    public static Response postRequest(String url, Object queryParams, Map<String, String> headers, Object payload) {
        return sendRequest("POST", url, queryParams, headers, payload);
    }


    @SneakyThrows
    public static String urlToString(String urlString, String compression) {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // Choose input stream based on compression type
        InputStream inputStream = connection.getInputStream();
        if ("gzip".equalsIgnoreCase(compression)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        String jsonString = byteArrayOutputStream.toString();
        return jsonString;
    }

//    public static <T> T parallelRun(int parallelism, Callable<T> codeBlock) {
//        ForkJoinPool customThreadPool = new ForkJoinPool(parallelism);
//        var result = customThreadPool.submit(codeBlock).join();
//        customThreadPool.shutdown();
//        return result;
//    }

    // TODO: a general method to handle http exception (429, timeout etc)


}
