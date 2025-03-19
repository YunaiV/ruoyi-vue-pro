package com.somle.microsoft.service;


import cn.iocoder.yudao.framework.common.util.json.JSONArray;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;

import com.somle.microsoft.model.MicrosoftClient;
import com.somle.microsoft.model.PowerbiAccount;
import com.somle.microsoft.model.PowerbiReportReqVO;
import com.somle.microsoft.model.PowerbiReportRespVO;
import com.somle.microsoft.repository.MicrosoftClientRepository;
import com.somle.microsoft.repository.PowerbiAccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import okhttp3.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class MicrosoftService {

    @Autowired
    private MessageChannel dataChannel;

    @Autowired
    private MicrosoftClientRepository microsoftClientRepository;

    @Autowired
    private PowerbiAccountRepository powerbiAccountRepository;

    private MicrosoftClient microsoftClient;

    private PowerbiAccount powerbiAccount;

    private String token;

//    String token = getPasswordToken();
    @PostConstruct
    public void init() {
        microsoftClient = microsoftClientRepository.findAll().get(0);
        powerbiAccount = powerbiAccountRepository.findAll().get(0);
    }

//    @Scheduled(cron = "0 */30 * * * *")
    @Scheduled(initialDelay = 2000, fixedRate = 1800000)
    @SneakyThrows
    public void refreshPasswordToken() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "password")
                .add("resource", "https://analysis.chinacloudapi.cn/powerbi/api")
                .add("client_id", microsoftClient.getClientId())
                .add("client_secret", microsoftClient.getClientSecret())
                .add("username", powerbiAccount.getUsername())
                .add("password", powerbiAccount.getPassword())
                .build();
        Request request = new Request.Builder()
                .url("https://login.partner.microsoftonline.cn/common/oauth2/token")
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtilsX.parseObject(response.body().string(), JSONObject.class);
        this.token = jsonObject.getString("access_token");
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

        var jsonObject = JsonUtilsX.parseObject(response.body().string(), JSONObject.class);
        var value = jsonObject.getJSONArray("value");

        return value;
    }

    @SneakyThrows
    public JSONArray getReports(String groupId) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(String.format("https://api.powerbi.cn/v1.0/myorg/groups/%s/reports", groupId))
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtilsX.parseObject(response.body().string(), JSONObject.class);
        var value = jsonObject.getJSONArray("value");
        log.info(value.toString());

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
                .addHeader("Authorization", "Bearer " + token)
                .build();
        Response response = client.newCall(request).execute();
        String result;
        String bodyString = null;
        try {
            bodyString = response.body().string();
            var jsonObject = JsonUtilsX.parseObject(bodyString, JSONObject.class);
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
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();


        var jsonObject = JsonUtilsX.parseObject(response.body().string(), JSONObject.class);
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