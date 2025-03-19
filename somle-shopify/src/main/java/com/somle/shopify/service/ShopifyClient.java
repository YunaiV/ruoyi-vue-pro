package com.somle.shopify.service;


import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.shopify.model.ShopifyToken;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.Map;

// https://shopify.dev/docs/api/admin-rest/
@Slf4j
public class ShopifyClient {

    private final ShopifyToken token;

    private final String url;

    @Setter
    private OkHttpClient webClient;

    public ShopifyClient(ShopifyToken token) {
        this.token = token;
        this.url = String.format("https://%s.myshopify.com", token.getSubdomain());
        this.webClient = new OkHttpClient();
    }

    public Map<String, String> getHeaders() {
        return Map.of(
            "Content-Type", "application/json",
            "X-Shopify-Access-Token", token.getAccessToken()
        );
    }


    @SneakyThrows
    public JSONObject getShop() {
        var endpoint = "/admin/api/2021-07/shop.json";

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }

    @SneakyThrows
    public JSONObject getOrders() {
        var endpoint = "/admin/api/2024-10/orders.json?status=any";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }

    @SneakyThrows
    public JSONObject getProducts() {
        var endpoint = "/admin/api/2024-10/products.json";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }



    @SneakyThrows
    public JSONObject getPayouts() {
        var endpoint = "/admin/api/2024-10/shopify_payments/payouts.json";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }

    @SneakyThrows
    private Response sendRequest(RequestX request) {
        // Define the proxy details
        return webClient.newCall(WebUtils.toOkHttp(request)).execute();
    }




}
