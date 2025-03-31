package com.somle.esb.job.oms.product;

import com.somle.esb.enums.SalesPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncShopifyProductsJob extends SyncProductsJob {
    @Override
    public String execute(String param) throws Exception {

        syncShopProductProfile(SalesPlatform.SHOPIFY, syncOmsClientMap);

        return "sync shopify products success!";
    }
}
