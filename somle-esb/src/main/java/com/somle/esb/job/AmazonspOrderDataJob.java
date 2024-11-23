package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpOrderReqVO;
import com.somle.amazon.controller.vo.AmazonSpOrderRespVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;
import com.somle.esb.model.OssData;
import com.somle.framework.common.util.collection.PageUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.object.BeanUtils;
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
                        .createdAfter(beforeYesterdayFirstSecond)
                        .createdBefore(beforeYesterdayLastSecond)
                        .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                        .build();
                amazonService.spClient.streamOrder(shop.getSeller(), vo).forEach(page-> {
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

        return "data upload success";
    }
}