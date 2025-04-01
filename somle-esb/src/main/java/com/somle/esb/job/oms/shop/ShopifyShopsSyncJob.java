package com.somle.esb.job.oms.shop;

import com.somle.esb.converter.oms.ShopifyToOmsConverter;
import com.somle.esb.enums.oms.PlatformEnum;
import com.somle.shopify.service.ShopifyClient;
import com.somle.shopify.service.ShopifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShopifyShopsSyncJob extends ShopsSyncJob {

    @Resource
    ShopifyClient shopifyClient;

    @Resource
    ShopifyToOmsConverter shopifyToOmsConverter;

    @Override
    public String execute(String param) throws Exception {

        syncShops(shopifyToOmsConverter, shopifyClient.getShops());

        return "sync shopify shops success!";
    }
}
