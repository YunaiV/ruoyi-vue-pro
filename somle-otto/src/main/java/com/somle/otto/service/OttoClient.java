package com.somle.otto.service;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.somle.otto.model.pojo.OttoAccount;
import com.somle.otto.model.pojo.OttoAuthToken;
import com.somle.otto.model.req.OttoReceiptReq;
import com.somle.otto.model.resp.*;
import com.somle.otto.repository.OttoAccountDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class OttoClient {
    private static final String BASEURL = "https://api.otto.market";
    public OttoAccount ottoAccount;

    OttoAccountDao accountDao;

    // 构造函数初始化 OkHttpClient
    public OttoClient(OttoAccount ottoAccount, OttoAccountDao accountDao) {
        this.ottoAccount = ottoAccount;
        this.accountDao = accountDao;
    }

    // 获取 OAuth 令牌
    public String getAccessToken() {
        return ottoAccount.getOauthToken().getAccessToken();
    }

    @SneakyThrows
    public OttoProductResp getProduct(String page) {
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/v4/products?page=" + page + "&limit=100")
            .headers(Map.of("Authorization", "Bearer " + getAccessToken()))
            .build();
        Response response = WebUtils.sendRequest(requestX);
        String bodyString = response.body().string();
        OttoProductResp ottoProductResp = JSONUtil.toBean(bodyString, OttoProductResp.class);
        return ottoProductResp;
    }


    public List<OttoProductResp.ProductVariation> getAllProducts() {
        refreshToken(this);
        List<OttoProductResp.ProductVariation> productVariations = new ArrayList<>();
        int page = 0;
        while (true) {
            OttoProductResp ottoProductResp = getProduct(String.valueOf(page));
            if (ottoProductResp.getProductVariations() == null || ottoProductResp.getProductVariations().isEmpty()) {
                break;
            }
            productVariations.addAll(ottoProductResp.getProductVariations());
            page++;
        }
        productVariations = productVariations.stream().map(
            productVariation -> {
                productVariation.setQuantity(getSkuQuantity(productVariation.getSku()).getQuantity());
                return productVariation;
            }
        ).toList();
        return productVariations;
    }

    @SneakyThrows
    public OttoSkuQuantityResp getSkuQuantity(String sku) {
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/v1/availability/quantities/" + sku)
            .headers(Map.of("Authorization", "Bearer " + getAccessToken()))
            .build();
        Response response = WebUtils.sendRequest(requestX);
        String bodyString = response.body().string();
        OttoSkuQuantityResp ottoSkuQuantityResp = JSONUtil.toBean(bodyString, OttoSkuQuantityResp.class);
        return ottoSkuQuantityResp;
    }

    /**
     * 获取订单
     * 按履行状态筛选的订单列表-递归获取,
     *
     * @param startTime 2024-12-01
     * @param endTime   2024-12-02
     * @return OttoCommonResp<Object> resources为订单页集合
     */
    @SneakyThrows
    public OttoOrderResp getOrders(String startTime, String endTime, String nextcursor) {

        Map<String, ? extends Serializable> map = Map.of(
            "fromOrderDate", startTime,
            "toOrderDate", endTime,
            "limit", 128,
//            "mode", "BUCKET",
            "nextcursor", nextcursor
        );
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/v4/orders")
            .queryParams(map)
            .headers(Map.of("Authorization", "Bearer " + getAccessToken()))
            .build();
        Response response = WebUtils.sendRequest(requestX);
        String bodyString = response.body().string();
        OttoOrderResp ottoOrderResp = JSONUtil.toBean(bodyString, OttoOrderResp.class);
        return ottoOrderResp;
    }

    @SneakyThrows
    public List<OttoOrder> getAllOrders(String startTime, String endTime) {
        refreshToken(this);
        List<OttoOrder> orders = new ArrayList<>();
        OttoOrderResp ottoOrderResp = getOrders(startTime, endTime, "");
        orders.addAll(ottoOrderResp.getResources());
        if (ottoOrderResp.getLinks() == null) {
            return orders;
        }
        OttoOrderResp.Link link = ottoOrderResp.getLinks().get(0);
        while ("next".equals(link.rel)) {
            ottoOrderResp = getOrders(startTime, endTime, getNextCursor(link.href).get());
            orders.addAll(ottoOrderResp.getResources());
            if (ottoOrderResp.getLinks() == null) {
                return orders;
            }
            link = ottoOrderResp.getLinks().get(0);
        }
        return orders;
    }

    public Optional<String> getNextCursor(String url) {
        if (url == null || !url.contains("?")) {
            return Optional.empty();
        }

        String query = url.split("\\?", 2)[1]; // 取查询参数部分
        for (String param : query.split("&")) {
            if (param.startsWith("nextcursor=")) {
                String[] pair = param.split("=", 2); // 分割键值对
                if (pair.length > 1) {
                    return Optional.of(pair[1]); // 返回值
                }
                return Optional.empty(); // 无值情况
            }
        }
        return Optional.empty(); // 未找到参数
    }

    /**
     * 获取收据(发票)
     * 递归获取-时间跨度不能太大，量多太慢
     *
     * @param startTime 2024-12-01
     * @param endTime   2024-12-02
     * @return OttoCommonResp<Object> resources为收据页集合
     */
    @SneakyThrows
    public OttoCommonResp<Object> getSettlement(String startTime, String endTime) {
        OttoReceiptReq req = OttoReceiptReq.builder()
            .limit(128)  // 最大128
            .from(startTime)
            .to(endTime)
            .receiptTypes(List.of("PURCHASE"))  // 订单完成并已支付
            .build();

        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/v3/receipts")
            .queryParams(req)
            .headers(Map.of("Authorization", "Bearer " + getAccessToken()))
            .build();

        try (Response response = WebUtils.sendRequest(requestX)) {
            return handleResponse(response);
        }
    }

    // 根据链接获取更多结算数据
    private OttoCommonResp<Object> getSettlementFromLinks(OttoCommonResp.OttoLinks links) {

        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + links.getHref())  // 使用 links 中的 href 字段
            .headers(Map.of("Authorization", "Bearer " + getAccessToken()))
            .build();

        try {
            try (Response response = WebUtils.sendRequest(requestX)) {
                if (response.isSuccessful()) {
                    return parseResponse(response);
                }
            }
            log.error("Failed to fetch data from link: {}", links.getHref());
        } catch (IOException e) {
            log.error("Error while fetching data from link: {}", links.getHref(), e);
        }
        return null;
    }

    // 递归处理链接
    private void handleLinksRecursively(OttoCommonResp<Object> commonResp) {
        if (commonResp.links != null && !commonResp.links.isEmpty()) {
            OttoCommonResp.OttoLinks link = commonResp.getLinks().get(0);
            log.info("Links found, fetching additional data from: {}", link.getHref());
            OttoCommonResp<Object> additionalData = getSettlementFromLinks(link);
            if (additionalData != null) {
                commonResp.resources.addAll(additionalData.getResources());
                commonResp.links = additionalData.getLinks();
                handleLinksRecursively(commonResp);  // 递归处理
            }
        }
    }

    // 处理 Response
    private OttoCommonResp<Object> handleResponse(Response response) throws IOException {
        if (response.code() == 200) {
            return parseResponse(response);
        } else {
            handleErrorResponse(response);
            return null;
        }
    }

    // 解析响应
    private OttoCommonResp<Object> parseResponse(Response response) throws IOException {
        //new TypeReference<OttoCommonResp<Object>>()
        OttoCommonResp<Object> commonResp = WebUtils.parseResponse(response, new TypeReference<>() {
        });
        handleLinksRecursively(commonResp);
        return commonResp;
    }

    // 错误响应处理
    private void handleErrorResponse(Response response) {
        switch (response.code()) {
            case 401:
                log.error("Unauthorized access - Please check your access token");
                break;
            case 403:
                log.error("Forbidden access - You do not have permission to access this resource");
                break;
            case 404:
                log.warn("Receipts not found for the specified date range");
                break;
            default:
                log.error("Unexpected error: HTTP code {}", response.code());
        }
        throw new RuntimeException("HTTP error: " + response.code());
    }


    // 刷新令牌
    public void refreshToken(OttoClient client) {
        OttoAccessTokenResp token = getToken(client);

        OttoAuthToken authToken = OttoAuthToken.builder()
            .scope(token.getScope())
            .accessToken(token.getAccessToken())
            .tokenType(token.getTokenType())
            .expiresIn(token.getExpiresIn())
            .build();

        log.debug("Generated authToken: {}", authToken);
        client.ottoAccount.setOauthToken(authToken);
        accountDao.save(client.ottoAccount);
    }

    @SneakyThrows
    private OttoAccessTokenResp getToken(OttoClient client) {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("scope", "orders products receipts returns price-reduction shipments quantities shipping-profiles availability")
            .add("client_id", client.ottoAccount.getClientId())
            .add("client_secret", client.ottoAccount.getClientSecret())
            .build();

        Request request = new Request.Builder()
            .url("https://api.otto.market/v1/token")
            .post(formBody)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return Optional.ofNullable(response.body())
                .map(responseBody -> {
                    try {
                        String bodyString = responseBody.string();
                        log.debug("Response body: {}", bodyString);
                        return JsonUtilsX.parseObject(bodyString, OttoAccessTokenResp.class);
                    } catch (IOException e) {
                        log.error("Error reading response body", e);
                        throw new RuntimeException("Error reading response body", e);
                    }
                })
                .orElseThrow(() -> {
                    log.error("Response body is null");
                    return new RuntimeException("Response body is null");
                });
        } catch (IOException e) {
            log.error("Error during token request", e);
            throw new RuntimeException("Error during token request", e);
        }
    }
}
