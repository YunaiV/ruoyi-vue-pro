package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspOrderDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        for (var client : amazonSpService.clients) {
            client.getMarketplaceParticipations().stream()
                .forEach(marketplaceParticipation -> {
                    var vo = AmazonSpOrderReqVO.builder()
                        .createdAfter(beforeYesterdayFirstSecond)
                        .createdBefore(beforeYesterdayLastSecond)
                        .marketplaceIds(List.of(marketplaceParticipation.getMarketplace().getId()))
                        .build();
                    client.streamOrder(vo).forEach(page-> {
                        var data = OssData.builder()
                            .database(DATABASE)
                            .tableName("order_create")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(beforeYesterday)
                            .content(page)
                            .headers(null)
                            .build();
                        service.send(data);
                    });

                });
        }


        return "data upload success";
    }
}