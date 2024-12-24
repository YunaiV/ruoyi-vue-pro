package com.somle.home24.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.somle.framework.common.util.web.RequestX;
import com.somle.framework.common.util.web.WebUtils;
import com.somle.home24.model.pojo.Home24Account;
import com.somle.home24.model.req.Home24InvoicesReq;
import com.somle.home24.model.req.Home24OrderReq;
import com.somle.home24.model.resp.Home24CommonRespInvoices;
import com.somle.home24.model.resp.Home24CommonRespOrders;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class Home24Client {
    private static final String BASEURL = "https://home24.mirakl.net";
    private Home24Account account;

    Home24Client(Home24Account home24Account) {
        this.account = home24Account;
    }

    public String getAccessToken() {
        return account.getApiKey();
    }

    //获取订单
    public Home24CommonRespOrders<Object> getOrder(Home24OrderReq orderReq) {

        // 初始化响应对象
        Home24CommonRespOrders<Object> resp = null;
        int offset = 0; // 从偏移量0开始
        int totalCount = 0; // 总数据量

        do {
            orderReq.setOffset(offset);
            Response response = WebUtils.sendRequest(createRequest(orderReq, "/api/orders"));
            Home24CommonRespOrders<Object> respOrders = handleResponse(response, Home24CommonRespOrders.class);

            if (resp == null) {
                resp = respOrders;
                assert resp != null;
                totalCount = resp.getTotal_count();

                // 确保 orders 列表已经初始化
                if (resp.getOrders() == null) {
                    resp.setOrders(new ArrayList<>()); // 初始化为空列表
                }
            } else {
                // 合并订单
                assert respOrders != null;
                if (respOrders.getOrders() != null) {
                    resp.getOrders()
                            .addAll(respOrders.getOrders());
                }
            }

            // 更新偏移量
            offset += orderReq.getMax();

        } while (offset < totalCount); // 如果当前的 offset 小于总数量，则继续请求

        return resp;
    }


    //获取发票
    public Home24CommonRespInvoices<Object> getInvoices(Home24InvoicesReq invoicesReq) {

        // 初始化响应对象
        Home24CommonRespInvoices<Object> resp = null;
        int offset = 0; // 从偏移量0开始
        int totalCount = 0; // 总数据量

        do {
            invoicesReq.setOffset(offset);
            Response response = WebUtils.sendRequest(createRequest(invoicesReq, "/api/invoices"));
            Home24CommonRespInvoices<Object> respInvoices = handleResponse(response, Home24CommonRespInvoices.class);

            if (resp == null) {
                resp = respInvoices;
                assert resp != null;
                totalCount = resp.getTotal_count();

                // 确保 orders 列表已经初始化
                if (resp.getInvoices() == null) {
                    resp.setInvoices(new ArrayList<>()); // 初始化为空列表
                }
            } else {
                // 合并订单
                assert respInvoices != null;
                if (respInvoices.getInvoices() != null) {
                    resp.getInvoices()
                            .addAll(respInvoices.getInvoices());
                }
            }

            // 更新偏移量
            offset += invoicesReq.getMax();

        } while (offset < totalCount); // 如果当前的 offset 小于总数量，则继续请求

        return resp;
    }

    /**
     * 创建请求
     *
     * @param req      请求参数
     * @param endpoint 请求地址
     * @return RequestX
     */
    private RequestX createRequest(Object req, String endpoint) {
        return RequestX.builder()
                .requestMethod(RequestX.Method.GET)
                .url(BASEURL + endpoint)
                .queryParams(req)
                .headers(Map.of("Authorization", getAccessToken()))
                .build();
    }

    /**
     * 处理 Response
     *
     * @param response  响应
     * @param respClass 响应类型类
     * @return Home24CommonResp<Object> //后续可以替换Object，给予映射类，Order等
     */
    private <T> T handleResponse(Response response, Class<T> respClass) {
        // 如果响应码为 200，解析响应内容
        if (response.code() == 200) {
            return parseResponse(response, respClass);
        } else {
            // 错误响应时处理
            handleErrorResponse(response);
            return null;
        }
    }


    /**
     * 处理 Response，支持多种类型的响应
     *
     * @param response  响应
     * @param respClass 响应类类型
     * @return 解析后的响应对象
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T parseResponse(Response response, Class<T> respClass) {
        if (response.body() == null) {
            throw new RuntimeException("Response 为空");
        }

        String jsonResponse = response.body()
                .string();

        // 解析不同的响应类型
        if (respClass == Home24CommonRespOrders.class) {
            return (T) WebUtils.parseResponse(jsonResponse, new TypeReference<Home24CommonRespOrders<Object>>() {
            });
        } else if (respClass == Home24CommonRespInvoices.class) {
            return (T) WebUtils.parseResponse(jsonResponse, new TypeReference<Home24CommonRespInvoices<Object>>() {
            });
        } else {
            throw new IllegalArgumentException("Unknown response type: " + respClass);
        }
    }


    // 错误响应处理
    private void handleErrorResponse(Response response) {
        int statusCode = response.code();
        String errorMessage = switch (statusCode) {
            case 400 -> "Bad Request - Parameter errors or bad method usage.";
            case 401 -> "Unauthorized - API call without authentication.";
            case 403 -> "Forbidden - Access to the resource is denied.";
            case 404 -> "Not Found - The resource does not exist.";
            case 405 -> "Method Not Allowed - The HTTP method is not allowed.";
            case 406 -> "Not Acceptable - The requested response content type is not available.";
            case 410 -> "Gone - The resource is permanently gone.";
            case 415 -> "Unsupported Media Type - The entity content type sent to the server is not supported.";
            case 429 -> "Too Many Requests - Rate limits are exceeded.";
            case 500 -> "Internal Server Error - The server encountered an unexpected error.";
            default -> "Unexpected error: HTTP code " + statusCode;
        };

        log.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }


}
