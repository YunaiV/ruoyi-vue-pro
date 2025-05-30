package com.somle.tiktok.service;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.alibaba.fastjson.JSON;
import com.somle.tiktok.model.TikTokAccount;
import com.somle.tiktok.model.resp.TikTokAuthResp;
import com.somle.tiktok.model.resp.TikTokShopResp;
import com.somle.tiktok.sdk.api.AuthorizationV202309Api;
import com.somle.tiktok.sdk.api.OrderV202309Api;
import com.somle.tiktok.sdk.api.ProductV202309Api;
import com.somle.tiktok.sdk.invoke.ApiClient;
import com.somle.tiktok.sdk.invoke.Configuration;
import com.somle.tiktok.sdk.model.Authorization.V202309.GetAuthorizedShopsResponse;
import com.somle.tiktok.sdk.model.Authorization.V202309.GetAuthorizedShopsResponseDataShops;
import com.somle.tiktok.sdk.model.Order.V202309.GetOrderListRequestBody;
import com.somle.tiktok.sdk.model.Order.V202309.GetOrderListResponse;
import com.somle.tiktok.sdk.model.Product.V202309.SearchProductsRequestBody;
import com.somle.tiktok.sdk.model.Product.V202309.SearchProductsResponse;
import com.somle.tiktok.sdk.model.Product.V202309.SearchProductsResponseDataProducts;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class TikTokClient {


    public String accessToken;
    private String TIKTOK_TOKEN_URL = "https://auth.tiktok-shops.com/api/v2/token/refresh";
    private String TIKTOK_URL = "https://open-api.tiktokglobalshop.com";
    private TikTokAccount tikTokAccount;

    public TikTokClient(TikTokAccount tikTokAccount) {
        this.tikTokAccount = tikTokAccount;
    }


    /**
     * https://auth.tiktok-shops.com/api/v2/token/get?app_key=6g6e8b2qd5ral
     * &app_secret=1b50daf5b5691ef5b602bc6845129c534f0677d4
     * &auth_code=ROW_SOuHfwAAAABOpSkQ8FsFmCReFBBoxvw3atypMGD4SDTx3zlB2kyJL9Jq41pn4xYzOXY96AKdHcYaZxdKRo8KeR3_gr3JEv8Cxdn3cA7dN1Bv4fOif-la_hIX4sFn6bdMAYdaFtw_yJ0OTozi15PG_yFu_cQM-fGE
     * &grant_type=authorized_code
     */
    @SneakyThrows
    public void getRefreshToken() {
        HashMap<String, String> params = new HashMap<>();
        params.put("app_key", tikTokAccount.getAppKey());
        params.put("app_secret", tikTokAccount.getAppSecret());
        params.put("auth_code", "ROW_6r1qGAAAAABOpSkQ8FsFmCReFBBoxvw3atypMGD4SDTx3zlB2kyJL9Jq41pn4xYzOXY96AKdHcYaZxdKRo8KeR3_gr3JEv8Cxdn3cA7dN1Bv4fOif-la_g-X-givLO-W3TdYlGDJJ5QgdTw10NzRlBy_sORbUSkP");
        params.put("grant_type", "authorized_code");
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url("https://auth.tiktok-shops.com/api/v2/token/get")
            .queryParams(params)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        System.out.println(bodyString);
    }


    @SneakyThrows
    public TikTokAuthResp getAccessToken(TikTokAccount tikTokAccount) {
        HashMap<String, String> params = new HashMap<>();
        params.put("app_key", tikTokAccount.getAppKey());
        params.put("app_secret", tikTokAccount.getAppSecret());
        params.put("refresh_token", tikTokAccount.getRefreshToken());
        params.put("grant_type", tikTokAccount.getGrantType());

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(TIKTOK_TOKEN_URL)
            .queryParams(params)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        TikTokAuthResp authResp = JSON.parseObject(bodyString, TikTokAuthResp.class);
        return authResp;
    }

    public Map<String, String> getHeaders() throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("x-tts-access-token", this.accessToken);
        return headers;
    }

    @SneakyThrows
    public List<TikTokShopResp.Shop> getShop() {
        ApiClient defaultClient = Configuration.getDefaultApiClient()
            .setAppkey(tikTokAccount.getAppKey())
            .setSecret(tikTokAccount.getAppSecret())
            .setBasePath(TIKTOK_URL);
        AuthorizationV202309Api apiInstance = new AuthorizationV202309Api(defaultClient);
        GetAuthorizedShopsResponse result = apiInstance.authorization202309ShopsGet(tikTokAccount.getAccessToken(), "application/json");
        List<GetAuthorizedShopsResponseDataShops> shops = result.getData().getShops();
        return BeanUtils.toBean(shops, TikTokShopResp.Shop.class);
    }


    public SearchProductsResponse getProduct(String nextPageToken) throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient()
            .setAppkey(tikTokAccount.getAppKey())
            .setSecret(tikTokAccount.getAppSecret())
            .setBasePath(TIKTOK_URL);
        ProductV202309Api apiInstance = new ProductV202309Api(defaultClient);
        SearchProductsRequestBody searchProductsRequestBody = new SearchProductsRequestBody();
        searchProductsRequestBody.setStatus("ALL");
        SearchProductsResponse result = apiInstance.product202309ProductsSearchPost(100, tikTokAccount.getAccessToken(), "application/json", nextPageToken, null, tikTokAccount.getShopCipher(), searchProductsRequestBody);
        return result;
    }

    @SneakyThrows
    public List<SearchProductsResponseDataProducts> getAllProducts() {
        List<SearchProductsResponseDataProducts> allProducts = new ArrayList<>();
        SearchProductsResponse searchProductsResponse = getProduct("");
        allProducts.addAll(searchProductsResponse.getData().getProducts());
        while (!"".equals(searchProductsResponse.getData().getNextPageToken())) {
            searchProductsResponse = getProduct(searchProductsResponse.getData().getNextPageToken());
            allProducts.addAll(searchProductsResponse.getData().getProducts());
        }
        return allProducts;
    }


    @SneakyThrows
    public void getOrder() {
        ApiClient defaultClient = Configuration.getDefaultApiClient()
            .setAppkey(tikTokAccount.getAppKey())
            .setSecret(tikTokAccount.getAppSecret())
            .setBasePath(TIKTOK_URL);
        OrderV202309Api apiInstance = new OrderV202309Api(defaultClient);
        GetOrderListRequestBody getOrderListRequestBody = new GetOrderListRequestBody();
        getOrderListRequestBody.setShippingType("TIKTOK");
        getOrderListRequestBody.setIsBuyerRequestCancel(false);
        GetOrderListResponse result = apiInstance.order202309OrdersSearchPost(20, this.accessToken, "application/json", "ASC", "", null, tikTokAccount.getShopCipher(), getOrderListRequestBody);
        System.out.println(result);
    }

}

