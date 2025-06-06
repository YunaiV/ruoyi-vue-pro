package com.somle.autonomous.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.somle.autonomous.model.AutonomousAccount;
import com.somle.autonomous.model.AutonomousAuthToken;
import com.somle.autonomous.req.AutonomousOrderReq;
import com.somle.autonomous.req.AutonomousProductReq;
import com.somle.autonomous.resp.AutonomousCommonResp;
import com.somle.autonomous.resp.AutonomousOrderResp;
import com.somle.autonomous.resp.AutonomousProductResp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AutonomousClient {
    private static final String BASEURL = "https://api-vendor.autonomous.ai";

    public AutonomousAccount autonomousAccount;

    public AutonomousClient(AutonomousAccount autonomousAccount) {
        this.autonomousAccount = autonomousAccount;
    }

    private Map<String, String> generateHeaders() {
        return Map.of("Authorization", "Bearer " + getAccessToken());
    }

    // 获取 OAuth 令牌
    public String getAccessToken() {
        return autonomousAccount.getAutonomousAuthToken()
            .getAccessToken();
    }

    //获取订单
    @SneakyThrows
    public AutonomousOrderResp getOrder(AutonomousOrderReq req) {
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/api/v1/order-detail")
            .headers(generateHeaders())
            .queryParams(req)
            .build();
        try (var response = WebUtils.sendRequest(requestX)) {
            var bodyString = response.body().string();
            AutonomousOrderResp autonomousOrderResp = JSON.parseObject(bodyString, AutonomousOrderResp.class);
            return autonomousOrderResp;

        }
    }

    public List<AutonomousOrderResp.OrderDetail> getAllOrder(String dateFrom, String dateTo) {
        AutonomousOrderReq req = AutonomousOrderReq.builder()
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .limit(50)
            .build();
        List<AutonomousOrderResp.OrderDetail> data = new ArrayList<>();
        AutonomousOrderResp orderResp = getOrder(req);
        data.addAll(orderResp.getData().getOrderDetails());
        while (CollectionUtil.isNotEmpty(orderResp.getData().getOrderDetails())) {
            List<AutonomousOrderResp.OrderDetail> orderDetails = orderResp.getData().getOrderDetails();
            String lastId = orderDetails.get(orderDetails.size() - 1).getOrderDetailId();
            req.setLastId(lastId);
            orderResp = getOrder(req);
            if (CollectionUtil.isNotEmpty(orderResp.getData().getOrderDetails())) {
                data.addAll(orderResp.getData().getOrderDetails());
            }
        }
        return data;
    }

    //获取已发货(排除退款+部分退款状态的订单，汇总可得结算价格（第三方平台客服干预下会强制部分退款，这部分暂不考虑，无法纳入测量）)
    public AutonomousOrderResp getSettlementOrder(AutonomousOrderReq req) {
        return getOrder(req);
    }


    public AutonomousAuthToken getAutonomousAuthTokenBySignIn(AutonomousAccount account) {
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .payload(account.setAutonomousAuthToken(null))
            .url(BASEURL + "/api/v1/auth/sign-in")
            .build();
        Response response = WebUtils.sendRequest(requestX);
        TypeReference<AutonomousCommonResp<AutonomousAuthToken>> typeRef = new TypeReference<>() {
        };
        AutonomousCommonResp<AutonomousAuthToken> autonomousCommonResp = handleResponse(response, typeRef);
        return autonomousCommonResp.getData();
    }

    @SneakyThrows
    private <T> T handleResponse(Response response, TypeReference<T> typeReference) {
        if (response.body() == null) {
            throw new RuntimeException("Response 为空");
        }
        if (response.isSuccessful()) {
            return WebUtils.parseResponse(response, typeReference);
        } else {
            // 错误处理逻辑
            String errorResponse = response.body()
                .string();
            throw new IllegalStateException("Failed to get valid response: " + errorResponse);
        }
    }

    @SneakyThrows
    public AutonomousProductResp getProduct(AutonomousProductReq vo) {
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/api/v1/product/list")
            .headers(generateHeaders())
            .queryParams(vo)
            .build();
        try (Response response = WebUtils.sendRequest(requestX)) {
            String bodyString = response.body().string();
            AutonomousProductResp autonomousProductResp = JSON.parseObject(bodyString, AutonomousProductResp.class);
            return autonomousProductResp;
        }
    }

    public List<AutonomousProductResp.Product> getAllProduct() {
        AutonomousProductReq vo = AutonomousProductReq.builder()
            .page(0)
            .limit(100)
            .build();
        List<AutonomousProductResp.Product> data = new ArrayList<>();
        AutonomousProductResp product = getProduct(vo);
        data.addAll(product.getData().getData());
        while (CollectionUtil.isNotEmpty(product.getData().getData())) {
            vo.setPage(vo.getPage() + 1);
            product = getProduct(vo);
            data.addAll(product.getData().getData());
        }
        return data.stream()
            .filter(p -> p != null && CollectionUtil.isNotEmpty(p.getAliasSkus()))
            .collect(Collectors.toList());
    }

}
