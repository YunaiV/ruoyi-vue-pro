package com.somle.walmart.service;


import cn.iocoder.yudao.framework.common.util.io.IoUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.walmart.model.WalmartOrderReqVO;
import com.somle.walmart.model.WalmartToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public abstract class WalmartClient {


    WalmartToken token;

    private String accessToken;

    public WalmartClient(WalmartToken token) {
        this.token = token;
        this.accessToken = getAccessToken();
        log.info(token.getSvcName());
    }

    /**
     * 对密钥进行编码
     */
    private String getAuthorization(WalmartToken token) {
        String str = token.getClientId()+ ":" + token.getClientSecret();
        return "Basic " + Base64.encodeBase64String(str.getBytes());
    }

    Headers commonHeaders() {
        return new Headers.Builder()
                .add("WM_QOS.CORRELATION_ID", "b3261d2d-028a-4ef7-8602-633c23200af6")
                .add("WM_SVC.NAME", token.getSvcName())
                .add("Accept", "application/json")
                .build();

    }

    Headers normalHeaders() {
        return commonHeaders().newBuilder()
                .add("WM_SEC.ACCESS_TOKEN", getAccessToken())
                .build();
    }

    abstract Headers headers();

    abstract HttpUrl url(String endpoint);


    @SneakyThrows
    public String getAccessToken() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials");
        var url = url("v3/token");
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .headers(commonHeaders())
                .addHeader("Authorization", getAuthorization(token))
                .build();

        log.info(request.toString());
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        log.info(bodyString);
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result.getString("access_token");
    }


    @Scheduled(cron = "0 */7 * * * *")
    public void refreshAccessToken() {
        accessToken = getAccessToken();
    }

    @SneakyThrows
    public JSONObject getOrders(WalmartOrderReqVO vo) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        var url = url("v3/orders");
        var headers = WebUtils.merge(headers(), WebUtils.toHeaders(vo));
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .headers(headers)
                .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }

    @SneakyThrows
    public List<String> getAvailableReconFileDates() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        var url = url("v3/report/reconreport/availableReconFiles").newBuilder()
                .addQueryParameter("reportVersion", "v1")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .headers(headers())
                .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result.getStringList("availableApReportDates");
    }

    @SneakyThrows
    public String getReconFile(String date) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        var url = url("v3/report/reconreport/availableReconFiles").newBuilder()
                .addQueryParameter("reportVersion", "v1")
                .addQueryParameter("reportDate", date)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .headers(headers())
                .addHeader("Accept", "application/octet-stream")
                .build();
        Response response = client.newCall(request).execute();
        // Execute the request
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        // Get the Content-Disposition header
//        String contentDisposition = response.header("Content-Disposition");
//        if (contentDisposition == null || !contentDisposition.contains("filename=")) {
//            throw new IOException("Filename not found in Content-Disposition header");
//        }
//
//        String zipFileName = contentDisposition.split("filename=")[1];
//        log.info("zip file name: " + zipFileName);

        // Save the response body as a zip file
        InputStream inputStream = response.body().byteStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;
        String result = null;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".csv")) {
                result = IoUtils.readUtf8(zipInputStream, false);
            }
            zipInputStream.closeEntry();
        }

        return result;
    }

    public String getReconFile(LocalDate date) {
        var formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        return getReconFile(date.format(formatter));
    }

    @SneakyThrows
    public JSONObject getPaymentStatement() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        var url = url("v3/report/payment/statement");
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .headers(headers())
                .build();
        Response response = client.newCall(request).execute();
        var bodyString = response.body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }
}