package com.somle.home24.service;

import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.somle.home24.model.pojo.Home24Account;
import com.somle.home24.model.req.Home24InvoicesReq;
import com.somle.home24.model.req.Home24OrderReq;
import com.somle.home24.model.resp.Home24CommonInvoicesResp;
import com.somle.home24.model.resp.Home24CommonOrdersResp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class Home24Client {
    private static final String BASEURL = "https://home24.mirakl.net";
    private final Home24Account account;

    Home24Client(Home24Account home24Account) {
        this.account = home24Account;
    }

    public String getAccessToken() {
        return account.getApiKey();
    }

    // 获取订单-通过offset递归http
    public Home24CommonOrdersResp<Object> getOrder(Home24OrderReq orderReq) {
        return getResp(orderReq);
    }

    @Nullable
    private Home24CommonOrdersResp<Object> getResp(Home24OrderReq orderReq) {
        Home24CommonOrdersResp<Object> home24Resp = null;
        int offset = 0;
        int totalCount = 0;

        do {
            // 设置偏移量
            orderReq.setOffset(offset);
            AtomicReference<Home24CommonOrdersResp<Object>> respOrders = new AtomicReference<>();
            // 发送请求并处理响应
            WebUtils.sendRequest(createRequest(orderReq, "/api/orders", RequestX.Method.GET), (response, e) -> {
                if (e != null) {
                    log.error("请求失败 {}", e.getMessage());
                    try {
                        throw e;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    respOrders.set(handleResponse(response, Home24CommonOrdersResp.class));
                }
            });


            // 如果这是第一次请求响应，初始化响应对象
            if (home24Resp == null && respOrders.get() != null) {
                home24Resp = respOrders.get();
                totalCount = home24Resp.getTotal_count();

                // 确保 orders 列表已经初始化
                if (home24Resp.getOrders() == null) {
                    home24Resp.setOrders(new ArrayList<>()); // 初始化为空列表
                }
            } else if (home24Resp != null & respOrders.get() != null && respOrders.get().getOrders() != null) {
                //合并Order
                home24Resp.getOrders()
                    .addAll(respOrders.get().getOrders());
            }


            // 更新偏移量
            offset += orderReq.getMax();

        } while (offset < totalCount); // 如果当前的 offset 小于总数量，则继续请求

        return home24Resp;
    }


    // 获取发票-通过offset递归http
    public Home24CommonInvoicesResp<Object> getInvoices(Home24InvoicesReq invoicesReq) {
        Home24CommonInvoicesResp<Object> resp = null;
        int offset = 0;
        int totalCount = 0;

        do {
            // 设置偏移量
            invoicesReq.setOffset(offset);

            // 发送请求并处理响应
            AtomicReference<Home24CommonInvoicesResp<Object>> respInvoices = new AtomicReference<>();
            RequestX request = createRequest(invoicesReq, "/api/invoices", RequestX.Method.GET);
            WebUtils.sendRequest(request, (res, e) -> {
                if (e != null) {
                    log.error("请求失败 {}", e.getMessage());
                    try {
                        throw e;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    respInvoices.set(handleResponse(res, Home24CommonInvoicesResp.class));
                }
            });


            // 如果这是第一次请求响应，初始化响应对象
            if (resp == null && respInvoices.get() != null) {
                resp = respInvoices.get();
                totalCount = resp.getTotal_count();

                // 确保 invoices 列表已经初始化
                if (resp.getInvoices() == null) {
                    resp.setInvoices(new ArrayList<>()); // 初始化为空列表
                }
            } else if (resp != null & respInvoices.get() != null && respInvoices.get().getInvoices() != null) {
                // 合并发票数据
                resp.getInvoices()
                    .addAll(respInvoices.get().getInvoices());
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
    private RequestX createRequest(Object req, String endpoint, RequestX.Method method) {
        return RequestX.builder()
            .requestMethod(method)
            .url(BASEURL + endpoint)
            .headers(Map.of("Authorization", getAccessToken()))
            .queryParams(req)
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
        // 解析不同的响应类型
        if (respClass == Home24CommonOrdersResp.class) {
            return (T) WebUtils.parseResponse(response, new TypeReference<Home24CommonOrdersResp<Object>>() {
            });
        } else if (respClass == Home24CommonInvoicesResp.class) {
            return (T) WebUtils.parseResponse(response, new TypeReference<Home24CommonInvoicesResp<Object>>() {
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
