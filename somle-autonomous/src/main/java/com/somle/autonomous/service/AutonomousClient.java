package com.somle.autonomous.service;

import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.somle.autonomous.model.AutonomousAccount;
import com.somle.autonomous.model.AutonomousAuthToken;
import com.somle.autonomous.req.AutonomousOrderReq;
import com.somle.autonomous.resp.AutonomousCommonResp;
import com.somle.autonomous.resp.AutonomousOrderResp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AutonomousClient {
    private static final String BASEURL = "https://api-vendor.autonomous.ai";

    AutonomousAccount autonomousAccount;

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
    public AutonomousOrderResp getOrder(AutonomousOrderReq req) {
        RequestX requestX = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASEURL + "/api/v1/order-detail")
            .headers(generateHeaders())
            .queryParams(req)
            .build();
        AtomicReference<AutonomousOrderResp> autonomousOrderResp = new AtomicReference<>();

        WebUtils.sendRequest(requestX, (response, e) -> {
            if (e != null) {
                log.error("response = {}", response);
                //抛出e
                try {
                    throw e;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                TypeReference<AutonomousCommonResp<AutonomousOrderResp>> typeRef = new TypeReference<>() {
                };
                autonomousOrderResp.set(handleResponse(response, typeRef).getData());
            }
        });
        return autonomousOrderResp.get();
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

}
