package com.somle.esb.job;


import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.walmart.model.WalmartOrderReqVO;
import com.somle.walmart.service.WalmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalmartOrderDataJob extends WalmartDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = WalmartOrderReqVO.builder()
                .createdStartDate(yesterdayFirstSecond)
                .createdEndDate(yesterdayLastSecond)
                .build();
        var result = walmartService.getClient().getOrders(vo);
        var data = OssData.builder()
                .database(DATABASE)
                .tableName("order")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(yesterday)
                .content(result)
                .headers(null)
                .build();
        service.send(data);

        return "data upload success";
    }
}