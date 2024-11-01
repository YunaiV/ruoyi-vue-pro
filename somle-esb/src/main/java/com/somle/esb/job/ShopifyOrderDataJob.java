package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.esb.model.OssData;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShopifyOrderDataJob extends ShopifyDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var result = shopifyService.client.getOrders();
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