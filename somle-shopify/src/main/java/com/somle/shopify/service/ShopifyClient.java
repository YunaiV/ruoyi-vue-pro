package com.somle.shopify.service;


import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.shopify.model.ShopifyToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ShopifyClient {

    private final ShopifyToken token;

    private final String url;

    public ShopifyClient(ShopifyToken token) {
        this.token = token;
        // TODO: figure out why https not succeed
        this.url = String.format("http://%s.myshopify.com", token.getSubdomain());
    }


    @SneakyThrows
    public JSONObject getShop() {
        var endpoint = "/admin/api/2021-07/shop.json";
        var headers = Map.of(
            "Content-Type", "application/json",
            "X-Shopify-Access-Token", token.getAccessToken()
        );
        var bodyString = WebUtils.getRequest(url+endpoint, Map.of(), headers).body().string();
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
        var bodyString = WebUtils.getRequest(url+endpoint, Map.of(), headers).body().string();
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
        var bodyString = WebUtils.getRequest(url+endpoint, Map.of(), headers).body().string();
        var result = JsonUtils.parseObject(bodyString, JSONObject.class);
        return result;
    }




}
