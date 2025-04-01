package com.somle.esb.job.oms.shop;

import com.somle.esb.enums.oms.SalesPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncShopifyShopsJob extends SyncShopsJob {

    @Override
    public String execute(String param) throws Exception {

        syncShopProfile(SalesPlatformEnum.SHOPIFY, syncOmsClientMap);

        return "sync shopify shops success!";
    }
}
