package com.somle.esb.job.oms.product;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.esb.converter.oms.ShopifyToOmsConverter;
import com.somle.esb.enums.TenantId;
import com.somle.shopify.service.ShopifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class ShopifyShopProductsSyncJob extends ShopProductsSyncJob {

    @Resource
    ShopifyService shopifyService;

    @Resource
    ShopifyToOmsConverter shopifyToOmsConverter;

    @Override
    public String execute(String param) throws Exception {
        // 设置租户为默认租户
        TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
        shopifyService.getAllShopifyClients().forEach(shopifyClient -> {
            syncShopProducts(shopifyToOmsConverter, shopifyClient.getProducts(new HashMap<>()),shopifyClient);
        });
        return "sync shopify shopProducts success!";
    }
}
