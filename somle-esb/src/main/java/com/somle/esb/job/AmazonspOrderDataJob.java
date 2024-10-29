package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspOrderDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        amazonService.spClient.getShops()
                .forEach(shop -> {
                    var vo = AmazonSpOrderReqVO.builder()
                            .CreatedAfter(beforeYesterdayFirstSecond)
                            .CreatedBefore(beforeYesterdayLastSecond)
                            .MarketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                            .build();
                    var report = amazonService.spClient.getOrder(shop.getSeller(), vo);
                    var data = OssData.builder()
                            .database(DATABASE)
                            .tableName("order")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(beforeYesterday)
                            .content(report)
                            .headers(null)
                            .build();
                    service.send(data);
                });

        return "data upload success";
    }
}