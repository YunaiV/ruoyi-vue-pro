package com.somle.microsoft.service;


import com.somle.framework.common.util.json.JSONArray;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;

import com.somle.microsoft.model.PowerbiReportReqVO;
import com.somle.microsoft.model.PowerbiReportRespVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class MicrosoftService {

    @Autowired
    private MessageChannel dataChannel;

//    String token = getPasswordToken();

    @SneakyThrows
    public String getPasswordToken() {

        String token = "";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=password&resource=https://analysis.chinacloudapi.cn/powerbi/api&client_id=8ad8fdb4-17bb-409a-b29c-0d5f2e168be4&client_secret=C.~XhRQ93V2msRuicF__4w43ViO-g223V5&username=general@somleNB.partner.onmschina.cn&password=Somle2023");
        Request request = new Request.Builder()
                .url("https://login.partner.microsoftonline.cn/common/oauth2/token")
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtils.parseObject(response.body().string(), JSONObject.class);

        token = jsonObject.getString("access_token");

        return token;
    }

    @SneakyThrows
    public JSONArray getGroups() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://api.powerbi.cn/v1.0/myorg/groups?groupDetails")
                .method("GET", body)
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtils.parseObject(response.body().string(), JSONObject.class);
        var value = jsonObject.getJSONArray("value");

        return value;
    }

    @SneakyThrows
    public JSONArray getReports(String groupId) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(String.format("https://api.powerbi.cn/v1.0/myorg/groups/%s}/reports", groupId))
                .method("GET", body)
                .addHeader("Authorization", "Bearer " + getPasswordToken())
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtils.parseObject(response.body().string(), JSONObject.class);
        var value = jsonObject.getJSONArray("value");

        return value;
    }

    @SneakyThrows
    public String getEmbedUrl(String reportId) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        Request request = new Request.Builder()
                .url(String.format("https://api.powerbi.cn/v1.0/myorg/reports/%s", reportId))
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + getPasswordToken())
                .build();
        Response response = client.newCall(request).execute();
        String result;
        String bodyString = null;
        try {
            bodyString = response.body().string();
            log.error(bodyString);
            var jsonObject = JsonUtils.parseObject(bodyString, JSONObject.class);
            var embedUrl = jsonObject.getString("embedUrl");
            result = embedUrl;
        } catch (Exception e) {
            throw new RuntimeException(e.toString() + bodyString);
        }

        return result;
    }

    @SneakyThrows
    public String getEmbedToken(String groupId, String reportId) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "accessLevel=View");
        Request request = new Request.Builder()
                .url(String.format("https://api.powerbi.cn/v1.0/myorg/groups/%s/reports/%s/GenerateToken", groupId, reportId))
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + getPasswordToken())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtils.parseObject(response.body().string(), JSONObject.class);
        var token = jsonObject.getString("token");

        return token;

    }

    public PowerbiReportRespVO getEmbedReport(PowerbiReportReqVO param) {
        return PowerbiReportRespVO.builder()
                .groupId(param.getGroupId())
                .reportId(param.getReportId())
                .embedUrl(getEmbedUrl(param.getReportId()))
                .reportToken(getEmbedToken(param.getGroupId(), param.getReportId()))
                .build();
    }

}