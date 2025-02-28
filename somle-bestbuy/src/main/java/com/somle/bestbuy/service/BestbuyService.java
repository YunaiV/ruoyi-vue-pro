package com.somle.bestbuy.service;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.bestbuy.repository.BestbuyTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务类，用于与Bestbuy API进行交互。
 *
 * @author: Wqh
 * @date: 2024/12/13 13:49
 */
@Slf4j
@Service
public class BestbuyService {
    @Resource
    private BestbuyTokenRepository bestbuyTokenRepository;

    // 基础URL，指向Bestbuy的API端点
    private final static String BASE_URL = "https://marketplace.bestbuy.ca";
    // 访问令牌，用于身份验证
    private String token;

    @PostConstruct
    public void init() {
        token = bestbuyTokenRepository.findAll().get(0).getToken();
    }
    /**
     * 获取订单信息。
     *
     * @return 包含订单信息的JSONObject
     */
    public JSONObject getOrders() {
        String endpoint = "/api/orders";
        return executeRequestAndParseResponse(endpoint, null, RequestX.Method.GET);
    }

    /**
     * 执行HTTP请求并解析响应。
     *
     * @param endpoint   请求的端点
     * @param requestBody 请求体，可以为null
     * @param httpMethod  HTTP方法（GET, POST, PUT等）
     * @return 解析后的JSONObject
     * @throws RuntimeException 如果请求失败或响应体为空
     */
    private JSONObject executeRequestAndParseResponse(String endpoint, String requestBody, RequestX.Method httpMethod) {
        try (Response response = sendRequest(endpoint, requestBody, httpMethod)) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    return JsonUtilsX.parseObject(body.string(), JSONObject.class);
                } else {
                    throw new RuntimeException("响应体为空");
                }
            } else {
                throw new RuntimeException("请求失败：失败码->" + response.code());
            }
        } catch (Exception e) {
            throw new RuntimeException("请求过程中发生异常", e);
        }
    }

    /**
     * 发送HTTP请求。
     *
     * @param endpoint   请求的端点
     * @param requestBody 请求体，可以为null
     * @param httpMethod  HTTP方法（GET, POST等）
     * @return HTTP响应
     */
    private Response sendRequest(String endpoint, String requestBody, RequestX.Method httpMethod) {
        var request = RequestX.builder()
            .requestMethod(httpMethod)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .build();
        if (httpMethod.equals(HttpMethod.POST)) {
            request.setPayload(JsonUtilsX.parseObject(requestBody, JSONObject.class));
        }
        return WebUtils.sendRequest(request);
    }

    /**
     * 获取请求头信息。
     *
     * @return 包含授权信息和内容类型的请求头Map
     */
    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }
}
