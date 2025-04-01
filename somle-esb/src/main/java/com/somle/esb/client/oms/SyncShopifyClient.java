package com.somle.esb.client.oms;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.esb.enums.oms.SalesPlatformEnum;
import com.somle.shopify.model.ShopifyToken;
import com.somle.shopify.model.reps.ShopifyProductRepsVO;
import com.somle.shopify.model.reps.ShopifyShopRepsVO;
import com.somle.shopify.service.ShopifyClient;
import com.somle.shopify.service.ShopifyService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class SyncShopifyClient extends SyncOmsClient<JSONObject, JSONObject> {


    @Resource
    private ShopifyService shopifyService;

    List<ShopifyClient> shopifyClients;

    public SyncShopifyClient() {
        super(SalesPlatformEnum.SHOPIFY);
    }

    /**
     * @Author: gumaomao
     * @Date: 2025/03/31
     * @Description: 初始化获得所有shopifyClient
     * @return:
     */
    @PostConstruct
    public void init() {
        List<ShopifyToken> allShopifyTokens = shopifyService.getAllShopifyTokens();
        shopifyClients = allShopifyTokens.stream()
            .map(shopifyToken -> new ShopifyClient(shopifyToken))
            .collect(Collectors.toList());
    }

    /**
     * @Description: 获得shopify所有店铺信息
     * @return: @return {@link List }
     */
    public List getShops() {
        List<ShopifyShopRepsVO> allShops = shopifyClients.stream()
            .map(ShopifyClient::getShops)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return allShops;
    }

    /**
     * @Description: 获取shopify所有产品信息
     * @return: @return {@link List }
     */
    @Override
    public List getProducts() {
        List<ShopifyProductRepsVO> allProducts = shopifyClients.stream()
            .map(shopifyClient -> shopifyClient.getProducts(Collections.emptyMap()))
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return allProducts;
    }
}



