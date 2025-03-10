package com.somle.cdiscount.service;

import cn.hutool.http.ContentType;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.cdiscount.model.CdiscountToken;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Data
public class CdiscountClient {

    //Cdiscount平台API请求路径
    private final String URL = "https://api.octopia-io.net/seller/v2";
    //Cdiscount平台获取token的请求路径
    private final String TOKEN_URL = "https://auth.octopia-io.net/auth/realms/maas/protocol/openid-connect/token";

    private CdiscountToken token;

    public CdiscountClient(CdiscountToken token) {
        this.token = token;
    }

    public Map<String, String> getHeaders() throws IOException {
        return Map.of(
            "Content-Type", "application/json",
            "sellerId", token.getSellerId(),
            "Authorization", "Bearer " + token.getAccessToken()
        );
    }

    @SneakyThrows
    public JSONObject getOrders() {
        //Cdiscount平台订单查询接口路径
        var endpoint = "/orders";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endpoint)
            .queryParams(
                Map.of(
                    "salesChannelId", "CDISFR",
                    "shippingCountry", "Fr"
                )
            )
            .headers(getHeaders())
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }


    @SneakyThrows
    public String getAccessToken() {
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(TOKEN_URL)
            .payload(token)
            .queryParams(
                Map.of(
                    "Content-Type", "application/x-www-form-urlencoded"
                )
            )
            .contentType(ContentType.FORM_URLENCODED)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        JSONObject jsonObject = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return jsonObject.getString("access_token");
    }

    protected void refreshToken() {
        String accessToken = getAccessToken();
        token.setAccessToken(accessToken);
        log.info("tokens refreshed successfully");
    }
}
