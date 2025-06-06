package com.somle.staples.service;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.somle.lazada.model.LazadaAccount;
import com.somle.lazada.model.reps.*;
import com.somle.lazada.sdk.api.LazopClient;
import com.somle.lazada.sdk.api.LazopRequest;
import com.somle.lazada.sdk.api.LazopResponse;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
public class LazadaClient {

    private final String URL = "https://auth.lazada.com/rest";
    private final String SINGAPORE_URL = "https://api.lazada.sg/rest";
    private LazadaAccount lazadaAccount;

    public LazadaClient(LazadaAccount lazadaAccount) {
        this.lazadaAccount = lazadaAccount;
    }

    @SneakyThrows
    public LazadaRefreshTokenResp refreshToken() {
        LazopClient client = new LazopClient(URL, lazadaAccount.getAppKey(), lazadaAccount.getAppSecret());
        LazopRequest request = new LazopRequest();
        request.setApiName("/auth/token/refresh");
        request.addApiParameter("refresh_token", lazadaAccount.getRefreshToken());
        LazopResponse response = client.execute(request);
        String body = response.getBody();
        LazadaRefreshTokenResp refreshTokenResp = JsonUtilsX.parseObject(body, LazadaRefreshTokenResp.class);
        return refreshTokenResp;
    }

    @SneakyThrows
    public LazadaSellerResp.ResponseData getSeller() {
        LazopClient client = new LazopClient(SINGAPORE_URL, lazadaAccount.getAppKey(), lazadaAccount.getAppSecret());
        LazopRequest request = new LazopRequest();
        request.setApiName("/seller/get");
        request.setHttpMethod("GET");
        LazopResponse response = client.execute(request, lazadaAccount.getAccessToken());
        String body = response.getBody();
        LazadaSellerResp sellerResp = JsonUtilsX.parseObject(body, LazadaSellerResp.class);
        return sellerResp.getData();
    }

    @SneakyThrows
    public LazadaProductResp getProducts(Integer offset, Integer limit) {
        LazopClient client = new LazopClient(SINGAPORE_URL, lazadaAccount.getAppKey(), lazadaAccount.getAppSecret());
        LazopRequest request = new LazopRequest();
        request.setApiName("/products/get");
        request.setHttpMethod("GET");
        request.addApiParameter("offset", String.valueOf(offset));
        request.addApiParameter("limit", String.valueOf(limit));
        LazopResponse response = client.execute(request, lazadaAccount.getAccessToken());
        String body = response.getBody();
        LazadaProductResp productResp = JSONUtil.toBean(body, LazadaProductResp.class);
        return productResp;
    }

    @SneakyThrows
    public List<LazadaProductResp.ResponseData.Product> getAllProducts() {

        List<LazadaProductResp.ResponseData.Product> allProducts = new ArrayList<>();
        Integer limit = 50;
        LazadaProductResp productResp = getProducts(0, limit);
        allProducts.addAll(productResp.getData().getProducts());
        while (productResp.getData().getTotalProducts() > allProducts.size()) {
            productResp = getProducts(allProducts.size(), limit);
            allProducts.addAll(productResp.getData().getProducts());
        }
        return allProducts;
    }

    @SneakyThrows
    public LazadaOrderResp getOrders(Integer offset, Integer limit, LocalDateTime createdAfter) {
        LazopClient client = new LazopClient(SINGAPORE_URL, lazadaAccount.getAppKey(), lazadaAccount.getAppSecret());
        LazopRequest request = new LazopRequest();
        request.setApiName("/orders/get");
        request.setHttpMethod("GET");

//        request.addApiParameter("created_after", "2017-02-10T09:00:00+08:00");

        // 2. 添加时区偏移（+08:00）
        ZoneOffset offsetTime = ZoneOffset.of("+08:00");
        OffsetDateTime endOffsetDateTime = createdAfter.atOffset(offsetTime);

        // 3. 定义目标格式（包含时区偏移）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        // 4. 格式化输出
        String end = endOffsetDateTime.format(formatter);

        request.addApiParameter("created_after", end);
        request.addApiParameter("offset", String.valueOf(offset));
        request.addApiParameter("limit", String.valueOf(limit));
        LazopResponse response = client.execute(request, lazadaAccount.getAccessToken());
        String body = response.getBody();
        LazadaOrderResp orderResp = JSONUtil.toBean(body, LazadaOrderResp.class);
        return orderResp;
    }

    @SneakyThrows
    public LazadaOrderItemResp getOrderItems(List<String> orderIds) {
        LazopClient client = new LazopClient(SINGAPORE_URL, lazadaAccount.getAppKey(), lazadaAccount.getAppSecret());
        LazopRequest request = new LazopRequest();
        request.setApiName("/orders/items/get");
        request.setHttpMethod("GET");
        String orderIdsStr = "[" +
            orderIds.stream()
                .collect(Collectors.joining(", ")) +
            "]";
//        request.addApiParameter("order_ids", "[42922, 32793]");
        request.addApiParameter("order_ids", orderIdsStr);
        LazopResponse response = client.execute(request, lazadaAccount.getAccessToken());
        String body = response.getBody();
        LazadaOrderItemResp orderItemResp = JSONUtil.toBean(body, LazadaOrderItemResp.class);
        return orderItemResp;
    }

    @SneakyThrows
    public List<LazadaOrderResp.ResponseData.Order> getAllOrders(LocalDateTime createdBefore, LocalDateTime createdAfter) {
        List<LazadaOrderResp.ResponseData.Order> allOrders = new ArrayList<>();
        List<LazadaOrderItemResp.OrderData> allOrderItems = new ArrayList<>();
        Integer limit = 50;
        LazadaOrderResp orderResp = getOrders(0, limit, createdAfter);
        if (orderResp.getData().getCountTotal() == 0) {
            return allOrders;
        }
        allOrders.addAll(orderResp.getData().getOrders());
        List<String> orderIds = orderResp.getData().getOrders().stream().map(LazadaOrderResp.ResponseData.Order::getOrderId).collect(Collectors.toList());
        allOrderItems.addAll(getOrderItems(orderIds).getData());
        while (orderResp.getData().getCountTotal() > allOrders.size()) {
            orderResp = getOrders(allOrders.size(), limit, createdAfter);
            allOrders.addAll(orderResp.getData().getOrders());
            orderIds = orderResp.getData().getOrders().stream().map(LazadaOrderResp.ResponseData.Order::getOrderId).collect(Collectors.toList());
            allOrderItems.addAll(getOrderItems(orderIds).getData());
        }
        Map<String, List<LazadaOrderItemResp.OrderData>> orderItemMap = allOrderItems.stream()
            .collect(Collectors.groupingBy(
                item -> item.getOrderId(),  // 键提取函数
                Collectors.toList()                        // 下游收集器
            ));
        allOrders.forEach(order -> {
            order.setOrderItems(orderItemMap.get(order.getOrderId()).get(0).getOrderItems());
        });
        return allOrders;
    }
}
