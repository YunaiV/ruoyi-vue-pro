package com.somle.cdiscount.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.ContentType;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.alibaba.fastjson.JSON;
import com.somle.cdiscount.model.CdiscountToken;
import com.somle.cdiscount.model.req.OrderReqVO;
import com.somle.cdiscount.model.req.ProductReqVO;
import com.somle.cdiscount.model.resp.CdiscountOrderRespVO;
import com.somle.cdiscount.model.resp.CdiscountProductRespVO;
import com.somle.cdiscount.model.resp.CdiscountSellerRespVO;
import com.somle.cdiscount.util.LinkHeaderParser;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class CdiscountClient {

    //Cdiscount平台API请求路径
    private final String URL = "https://api.octopia-io.net/seller/v2";
    //Cdiscount平台获取token的请求路径
    private final String TOKEN_URL = "https://auth.octopia-io.net/auth/realms/maas/protocol/openid-connect/token";

    private CdiscountToken token;

    public CdiscountClient(CdiscountToken token) {
        this.token = token;
    }

    public Map<String, String> getHeaders() throws IOException {
        return Map.of(
            "Content-Type", "application/json",
            "sellerId", token.getSellerId(),
            "Authorization", "Bearer " + token.getAccessToken()
        );
    }

    @SneakyThrows
    public CdiscountOrderRespVO getOrders(OrderReqVO orderReqVO) {
        //Cdiscount平台订单查询接口路径
        var endpoint = "/orders";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endpoint)
            .queryParams(
                Map.of(
//                    "salesChannelId", "CDISFR",
//                    "shippingCountry", "Fr",
                    "pageIndex", orderReqVO.getPageIndex(),
                    "pageSize", orderReqVO.getPageSize(),
                    "createdAtMin", orderReqVO.getCreatedAtMin(),
                    "createdAtMax", orderReqVO.getCreatedAtMax()
                )
            )
            .headers(getHeaders())
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        CdiscountOrderRespVO result = JSON.parseObject(bodyString, CdiscountOrderRespVO.class);
        return result;
    }

    public List<CdiscountOrderRespVO.Order> getAllOrders(LocalDateTime startTime, LocalDateTime endTime) {
        var vo = OrderReqVO.builder()
            .createdAtMin(startTime)
            .createdAtMax(endTime)
            .pageIndex(1)
            .pageSize(100)
            .build();
        List<CdiscountOrderRespVO.Order> result = new ArrayList<>();
        CdiscountOrderRespVO cdiscountOrderRespVO = getOrders(vo);
        result.addAll(cdiscountOrderRespVO.getItems());
        while (cdiscountOrderRespVO.getItemsPerPage() > 0) {
            vo.setPageIndex(vo.getPageIndex() + 1);
            cdiscountOrderRespVO = getOrders(vo);
            result.addAll(cdiscountOrderRespVO.getItems());
        }
        return result;
    }

    @SneakyThrows
    public CdiscountSellerRespVO getSeller() {
        var endpoint = "/sellers";

        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endpoint)
            .headers(getHeaders())
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        CdiscountSellerRespVO cdiscountSellerRespVO = JSON.parseObject(bodyString, CdiscountSellerRespVO.class);
        return cdiscountSellerRespVO;
    }

    @SneakyThrows
    public CdiscountProductRespVO getProducts(ProductReqVO productReqVO) {
        var endpoint = "/products?cursor=" + productReqVO.getCursor() + "&limit=" + productReqVO.getLimit();
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(URL + endpoint)
//            .queryParams(
//                Map.of(
//                    "salesChannelId", "CDISFR",
//                    "shippingCountry", "Fr"
//                )
//            )
            .headers(getHeaders())
            .build();
        Response response = WebUtils.sendRequest(request);

        String link = response.headers().get("Link");
        Map<String, String> linkMap = LinkHeaderParser.parseLinkHeader(link);
        String cursor = linkMap.get("next");
        var bodyString = response.body().string();
        CdiscountProductRespVO result = JSON.parseObject(bodyString, CdiscountProductRespVO.class);
        result.setCursor(cursor);
        return result;
    }

    public List<CdiscountProductRespVO.Item> getAllProducts() {
        var vo = ProductReqVO.builder()
            .cursor("")
            .limit(100)
            .build();
        List<CdiscountProductRespVO.Item> result = new ArrayList<>();
        CdiscountProductRespVO cdiscountProductRespVO = getProducts(vo);
        result.addAll(cdiscountProductRespVO.getItems());
        while (cdiscountProductRespVO.getItemsPerPage() > 0) {
            vo.setCursor(cdiscountProductRespVO.getCursor());
            cdiscountProductRespVO = getProducts(vo);
            if (CollectionUtil.isNotEmpty(cdiscountProductRespVO.getItems())) {
                result.addAll(cdiscountProductRespVO.getItems());
            }
        }
        return result;
    }


    @SneakyThrows
    public String getAccessToken() {
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(TOKEN_URL)
            .payload(token)
            .queryParams(
                Map.of(
                    "Content-Type", "application/x-www-form-urlencoded"
                )
            )
            .contentType(ContentType.FORM_URLENCODED)
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        JSONObject jsonObject = JsonUtilsX.parseObject(bodyString, JSONObject.class);
        return jsonObject.getString("access_token");
    }

    protected void refreshToken() {
        String accessToken = getAccessToken();
        token.setAccessToken(accessToken);
        log.info("tokens refreshed successfully");
    }
}
