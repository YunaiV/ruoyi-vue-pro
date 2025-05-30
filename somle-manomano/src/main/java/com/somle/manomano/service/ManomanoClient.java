package com.somle.manomano.service;

import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;
import com.somle.manomano.model.ManomanoShop;
import com.somle.manomano.model.reps.OffersInfoRespVO;
import com.somle.manomano.model.reps.OrderQueryRepsVO;
import com.somle.manomano.model.req.OffersInfoReqVO;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class ManomanoClient {

    private ManomanoShop shop;

    public ManomanoClient(ManomanoShop shop) {
        this.shop = shop;
    }

    private static final String ORDERS_BASE_URL_V1 = "https://partnersapi.manomano.com/orders/v1";

    private static final String BASE_URL_V1 = "https://partnersapi.manomano.com/api/v1";


    public Map<String, String> getHeaders() throws IOException {
        return Map.of(
            "x-api-key", shop.getToken().getAppKey()
        );
    }

    @SneakyThrows
    public OrderQueryRepsVO getOrders(OffersInfoReqVO reqVO) {
        var endpoint = "/orders";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(ORDERS_BASE_URL_V1 + endpoint)
            .queryParams(
                Map.of(
                    "seller_contract_id", reqVO.getSellerContractId(),
                    "page", reqVO.getPage(),
                    "limit", reqVO.getLimit(),
                    "created_at_start", reqVO.getCreatedAtStart(),
                    "created_at_end", reqVO.getCreatedAtEnd()
                )
            )
            .headers(getHeaders())
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        OrderQueryRepsVO result = JsonUtilsX.parseObject(bodyString, OrderQueryRepsVO.class);
        return result;
    }

    @SneakyThrows
    public List<OrderQueryRepsVO.Order> getAllOrders(String startTime, String endTime) {
        List<OrderQueryRepsVO.Order> allOrders = new ArrayList<>();
        OffersInfoReqVO reqVO = OffersInfoReqVO.builder()
            .sellerContractId(shop.getContractId())
            .createdAtStart(startTime)
            .createdAtEnd(endTime)
            .page(1)
            .limit(50)
            .build();
        OrderQueryRepsVO orderQueryRepsVO = getOrders(reqVO);
        allOrders.addAll(orderQueryRepsVO.getContent());
        while (orderQueryRepsVO.getPagination().getPages() > orderQueryRepsVO.getPagination().getPage()) {
            reqVO.setPage(orderQueryRepsVO.getPagination().getPage() + 1);
            orderQueryRepsVO = getOrders(reqVO);
            allOrders.addAll(orderQueryRepsVO.getContent());
        }
        return allOrders;
    }

    @SneakyThrows
    public OffersInfoRespVO getOffersInfo(OffersInfoReqVO reqVO) {

        var endpoint = "/offer-information/offers";
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.GET)
            .url(BASE_URL_V1 + endpoint)
            .queryParams(
                Map.of(
                    "seller_contract_id", reqVO.getSellerContractId(),
                    "page", reqVO.getPage(),
                    "limit", reqVO.getLimit()

                )
            )
            .headers(getHeaders())
            .build();
        var bodyString = WebUtils.sendRequest(request).body().string();
        OffersInfoRespVO offersInfoRespVO = JsonUtilsX.parseObject(bodyString, OffersInfoRespVO.class);
        return offersInfoRespVO;
    }

    public List<OffersInfoRespVO.ContentDTO> getAllProducts() {
        List<OffersInfoRespVO.ContentDTO> allProducts = new ArrayList<>();
        OffersInfoReqVO reqVO = OffersInfoReqVO.builder()
            .sellerContractId(shop.getContractId())
            .page(1)
            .limit(100)
            .build();
        OffersInfoRespVO offersInfoRespVO = getOffersInfo(reqVO);
        allProducts.addAll(offersInfoRespVO.getContent());
        while (offersInfoRespVO.getPagination().getPages() > offersInfoRespVO.getPagination().getPage()) {
            reqVO.setPage(offersInfoRespVO.getPagination().getPage() + 1);
            offersInfoRespVO = getOffersInfo(reqVO);
            allProducts.addAll(offersInfoRespVO.getContent());
        }
        return allProducts;
    }

}