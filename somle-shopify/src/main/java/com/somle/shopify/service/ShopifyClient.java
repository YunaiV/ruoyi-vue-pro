package com.somle.shopify.service;


import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.shopify.model.ShopifyToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.util.Map;
import java.net.Proxy;

// https://shopify.dev/docs/api/admin-rest/
@Slf4j
public class ShopifyClient {

    private final ShopifyToken token;

    private final String url;

    public ShopifyClient(ShopifyToken token) {
        this.token = token;
        this.url = String.format("https://%s.myshopify.com", token.getSubdomain());
    }


    @SneakyThrows
    public JSONObject getShop() {
        var endpoint = "/admin/api/2021-07/shop.json";
        var headers = Map.of(
            "Content-Type", "application/json",
            "X-Shopify-Access-Token", token.getAccessToken()
        );
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(headers)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }

    @SneakyThrows
    public JSONObject getOrders() {
        var endpoint = "/admin/api/2024-10/orders.json?status=any";
        var headers = Map.of(
                "Content-Type", "application/json",
                "X-Shopify-Access-Token", token.getAccessToken()
        );
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(headers)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }

    @SneakyThrows
    public JSONObject getPayouts() {
        var endpoint = "/admin/api/2024-10/shopify_payments/payouts.json";
        var headers = Map.of(
                "Content-Type", "application/json",
                "X-Shopify-Access-Token", token.getAccessToken()
        );
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url+endpoint)
            .headers(headers)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }

    private void getRequest(String url, Object queryParams, Map<String, String> headers) {
        // Define the proxy details
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;

        // Create a Proxy instance
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

        OkHttpClient client = new OkHttpClient.Builder()
            .proxy(proxy)
            .proxyAuthenticator((route, response) -> {
                String credential = okhttp3.Credentials.basic("proxyUser", "proxyPassword");
                return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
            })
            .build();


    }




}
