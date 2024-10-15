package com.somle.microsoft.service;


import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.messaging.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class MicrosoftService {

    @Autowired
    private MessageChannel dataChannel;

    @SneakyThrows
    public String getPasswordToken() {

        String token = "";

//        String url = "https://login.partner.microsoftonline.cn/common/oauth2/token";
//
//        Map<String, String> header = new HashMap<>(16);
//        header.put("Content-Type", "application/x-www-form-urlencoded");
//        header.put("Accept", "*/*");
//
//        Map<String, String> body = new HashMap<>(16);
//        body.put("grant_type", "password");
//        body.put("resource", "https://analysis.chinacloudapi.cn/powerbi/api");
//        body.put("client_id", "8ad8fdb4-17bb-409a-b29c-0d5f2e168be4");
//        body.put("client_secret", "C.~XhRQ93V2msRuicF__4w43ViO-g223V5");
//        body.put("username", "general@somleNB.partner.onmschina.cn");
//        body.put("password", "Somle2023");
//
//        String baseStr = WebUtils.postRequest(url, Map.of(), header, body).body().string();
//
//        var jsonObject = JsonUtils.parseObject(baseStr, JSONObject.class);
//        log.error(jsonObject.toString());
//        token = jsonObject.getString("access_token");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=password&resource=https://analysis.chinacloudapi.cn/powerbi/api&client_id=8ad8fdb4-17bb-409a-b29c-0d5f2e168be4&client_secret=C.~XhRQ93V2msRuicF__4w43ViO-g223V5&username=general@somleNB.partner.onmschina.cn&password=Somle2023");
        Request request = new Request.Builder()
                .url("https://login.partner.microsoftonline.cn/common/oauth2/token")
                .method("POST", body)
                .addHeader("Accept", "*/*")
                .build();
        Response response = client.newCall(request).execute();

        var jsonObject = JsonUtils.parseObject(response.body().string(), JSONObject.class);

        token = jsonObject.getString("access_token");

        return token;
    }

}