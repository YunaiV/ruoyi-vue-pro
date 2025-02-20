package com.somle.otto.service;

import com.fasterxml.jackson.core.type.TypeReference;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.otto.model.pojo.OttoAccount;
import com.somle.otto.model.req.OttoReceiptReq;
import com.somle.otto.model.resp.OttoCommonResp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Slf4j
public class OttoClient {
    private static final String BASEURL = "https://api.otto.market";
    OttoAccount ottoAccount;

    // 构造函数初始化 OkHttpClient
    public OttoClient(OttoAccount ottoAccount) {
        this.ottoAccount = ottoAccount;
    }

    // 获取 OAuth 令牌
    public String getAccessToken() {
        return ottoAccount.getOauthToken().getAccessToken();
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
    public OttoCommonResp<Object> getOrders(String startTime, String endTime) {

        Map<String, ? extends Serializable> map = Map.of(
                "fromOrderDate", startTime,
                "toOrderDate", endTime,
                "limit", 128,
                "mode", "BUCKET"
        );
        RequestX requestX = RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(BASEURL + "/v4/orders")
                .queryParams(map)
                .headers(Map.of("Authorization", "Bearer " + getAccessToken()))
                .build();
        try(Response response = WebUtils.sendRequest(requestX)){
            return handleResponse(response);
        }
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

        try(Response response = WebUtils.sendRequest(requestX)){
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
            try(Response response = WebUtils.sendRequest(requestX)){
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

}
