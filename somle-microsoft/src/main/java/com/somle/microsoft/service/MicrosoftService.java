package com.somle.microsoft.service;


import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
        String url = "https://login.partner.microsoftonline.cn/common/oauth2/token";

        Map<String, String> header = new HashMap<>(16);
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("Accept", "*/*");

        Map<String, String> body = new HashMap<>(16);
        body.put("grant_type", "password");
        body.put("resource", "https://analysis.chinacloudapi.cn/powerbi/api");
        body.put("client_id", "8ad8fdb4-17bb-409a-b29c-0d5f2e168be4");
        body.put("client_secret", "C.~XhRQ93V2msRuicF__4w43ViO-g223V5");
        body.put("username", "general@somleNB.partner.onmschina.cn");
        body.put("password", "Somle2023");

        StringBuilder bodyStr = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : body.entrySet()) {
            i++;
            bodyStr.append(entry.getKey()).append("=").append(entry.getValue());
            if (i < body.size()) {
                bodyStr.append("&");
            }
        }

        String baseStr = WebUtils.postRequest(url, Map.of(), header, body).body().string();

        var jsonObject = JsonUtils.parseObject(baseStr, JSONObject.class);
        log.error(jsonObject.toString());
        token = jsonObject.getString("access_token");

        return token;
    }

}