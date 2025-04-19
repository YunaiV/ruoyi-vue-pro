package com.somle.esb.job;


import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class ShopifyOrderDataJob extends ShopifyDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var result = shopifyService.shopifyClients.get(0).getOrders();
        var data = OssData.builder()
            .database(DATABASE)
            .tableName("order")
            .syncType("inc")
            .requestTimestamp(System.currentTimeMillis())
            .folderDate(beforeYesterday)
            .content(result)
            .headers(null)
            .build();
        service.send(data);

        return "data upload success";
    }
}