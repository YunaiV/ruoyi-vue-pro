package com.somle.esb.job.oms.shop;

import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.job.oms.SyncOmsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SyncShopifyShopsJob extends SyncShopsJob {

    @Override
    public String execute(String param) throws Exception {

        // 获得所有注册的 ShopProfileClient 类型的 Spring Bean
        Map<String, SyncOmsClient> shopProfileClients = applicationContext.getBeansOfType(SyncOmsClient.class);
        Map<SalesPlatform,SyncOmsClient> shopProfileClientMap=shopProfileClients.values().stream()
            .collect(Collectors.toMap(t->t.getSalesPlatform(), t -> t));

        syncShopProfile(SalesPlatform.SHOPIFY,shopProfileClientMap);

        return "sync shopify shops success!";
    }
}
