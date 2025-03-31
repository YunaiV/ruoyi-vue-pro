package com.somle.shopify.service;

import cn.iocoder.yudao.framework.common.util.json.JSONArray;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.somle.shopify.enums.ShopifyAPI;
import com.somle.shopify.model.ShopifyToken;
import com.somle.shopify.model.reps.ShopifyProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: LeeFJ
 * @date: 2025/2/14 10:22
 * @description: Shopify 客户端，负责与 Shopify 服务进行交互
 * Shopify 接口文档 <br>
 * https://shopify.dev/docs/api/admin-rest/
 */
@Slf4j
public class ShopifyClient {


    // Header
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String SHOPIFY_ACCESS_TOKEN = "X-Shopify-Access-Token";
    // API
    public static final String BASE_URL = "https://%s.myshopify.com";

    private ShopifyToken token;

    private String url;

    @Setter
    private OkHttpClient webClient;

    public ShopifyClient(ShopifyToken token) {
        this.token = token;
        this.url = String.format(BASE_URL, token.getSubdomain());
        this.webClient = new OkHttpClient();
    }

    /**
     * 获得店铺信息
     **/
    public List<ShopifyShopRepsVO> getShops() {
        JSONObject shop = getResult(ShopifyAPI.GET_SHOP, new HashMap<>());
        List<ShopifyShopRepsVO> shops = new ArrayList<>();
        shops.add(JsonUtilsX.parseObject(shop, ShopifyShopRepsVO.class));
        return shops;
    }


    /**
     * 获得订单信息
     **/
    @SneakyThrows
    public JSONObject getOrders() {
        var endpoint = "/admin/api/2024-10/orders.json?status=any";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url + endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }

    /**
     * 获得商品信息
     **/
    public List<ShopifyProductRepsVO> getProducts(Map<String, ?> params) {
        JSONArray productArr = getResult(ShopifyAPI.GET_PRODUCTS, params);
        List<ShopifyProductRepsVO> products = new ArrayList<>();
        for (JsonNode productNode : productArr) {
            ShopifyProductRepsVO shopifyProductRepsVO = JsonUtilsX.parseObject(productNode, ShopifyProductRepsVO.class);
            products.add(shopifyProductRepsVO);
        }
        return products;
    }


    /**
     * 获得结算信息
     **/
    @SneakyThrows
    public JSONObject getPayouts() {
        var endpoint = "/admin/api/2024-10/shopify_payments/payouts.json";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(url + endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = sendRequest(request).body().string();
        var result = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return result;
    }

    /**
     * @return 返回原始报文
     * @Author LeeFJ
     * @Description
     * @Date 8:19 2025/2/8
     * @Param
     **/
    private JSONObject getRawResult(ShopifyAPI api, Map<String, ?> params) {
        Response response = null;
        try {
            var request = RequestX.builder()
                .requestMethod(api.method())
                .url(url + api.url())
                .headers(getHeaders())
                .queryParams(params)
                .build();
            response = sendRequest(request);
            var bodyString = response.body().string();
            return JsonUtilsX.parseObject(bodyString, JSONObject.class);
        } catch (Throwable t) {
            log.error("{}异常", api.action(), t);
            return null;
        } finally {
            if (response != null) {
                response.code();
            }
        }
    }

    /**
     * @return 返回有效的业务报文
     * @Author LeeFJ
     * @Description
     * @Date 8:19 2025/2/8
     * @Param
     **/
    private <T> T getResult(ShopifyAPI api, Map<String, ?> params) {
        var result = getRawResult(api, params);
        if (result == null) {
            return null;
        }
        return (T) api.getData(result, api.returnType());
    }

    private Map<String, String> getHeaders() {
        return Map.of(
            CONTENT_TYPE, APPLICATION_JSON,
            SHOPIFY_ACCESS_TOKEN, token.getAccessToken()
        );
    }

    @SneakyThrows
    private Response sendRequest(RequestX request) {
        // Define the proxy details
        return webClient.newCall(WebUtils.toOkHttp(request)).execute();
    }

}
