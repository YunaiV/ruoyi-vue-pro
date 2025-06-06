package com.somle.bestbuy.service;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.bestbuy.model.BestbuyToken;
import com.somle.bestbuy.req.BestbuyOrderReq;
import com.somle.bestbuy.req.BestbuyProductReq;
import com.somle.bestbuy.resp.BestbuyOrderRespVO;
import com.somle.bestbuy.resp.BestbuyShopRespVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BestbuyClient {

    // 基础URL，指向Bestbuy的API端点
    private final static String BASE_URL = "https://marketplace.bestbuy.ca";
    // 访问令牌，用于身份验证
    private String token;


    public BestbuyClient(BestbuyToken bestbuyToken) {
        this.token = bestbuyToken.getToken();
    }

    @SneakyThrows
    public BestbuyShopRespVO getShopInformation() {
        String endpoint = "/api/account";

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .build();

        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        BestbuyShopRespVO bestbuyShopRespVO = JSONUtil.toBean(bodyString, BestbuyShopRespVO.class);
        return bestbuyShopRespVO;
    }

    @SneakyThrows
    public String getProducts() {
        String endpoint = "/api/products?";
        var vo = BestbuyProductReq.builder()
            .productReferences("EAN")
            .build();
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASE_URL + endpoint + "product_references=" + vo.getProductReferences())
            .headers(getHeaders())
            .build();
        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        BestbuyShopRespVO bestbuyShopRespVO = JSONUtil.toBean(bodyString, BestbuyShopRespVO.class);
        return "111";
    }

    @SneakyThrows
    public BestbuyOrderRespVO getOrders(BestbuyOrderReq vo) {
        String endpoint = "/api/orders?max=" + vo.getMax() + "&offset=" + vo.getOffset() + "&start_date=" + vo.getStartDate() + "&end_date=" + vo.getEndDate();
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASE_URL + endpoint)
            .headers(getHeaders())
            .build();
        Response response = WebUtils.sendRequest(request);
        String bodyString = response.body().string();
        BestbuyOrderRespVO bestbuyOrderRespVO = JSONUtil.toBean(bodyString, BestbuyOrderRespVO.class);
        return bestbuyOrderRespVO;
    }

    @SneakyThrows
    public List<BestbuyOrderRespVO.Order> getAllOrders(LocalDateTime startTime, LocalDateTime endTime) {

        var vo = BestbuyOrderReq.builder()
            .max(10)
            .offset(0)
            .startDate(startTime)
            .endDate(endTime)
            .build();
        List<BestbuyOrderRespVO.Order> result = new ArrayList<>();
        BestbuyOrderRespVO bestbuyOrderRespVO = getOrders(vo);
        result.addAll(bestbuyOrderRespVO.getOrders());
        while (ObjectUtil.isNotEmpty(bestbuyOrderRespVO) &&
            CollectionUtil.isNotEmpty(bestbuyOrderRespVO.getOrders())) {
            vo.setOffset(vo.getOffset() + bestbuyOrderRespVO.getOrders().size());
            bestbuyOrderRespVO = getOrders(vo);
            if (ObjectUtil.isNotEmpty(bestbuyOrderRespVO) &&
                CollectionUtil.isNotEmpty(bestbuyOrderRespVO.getOrders())) {
                result.addAll(bestbuyOrderRespVO.getOrders());
            }
        }
        return result;
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
