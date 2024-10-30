package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.amazon.service.AmazonService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspAsinReportDataJob extends AmazonspDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var dataDate = beforeYesterday;

        amazonService.spClient.getShops().map(shop-> {
            var options = AmazonSpReportSaveVO.ReportOptions.builder()
                    .asinGranularity("CHILD")
                    .dateGranularity("DAY")
                    .build();
            var vo = AmazonSpReportSaveVO.builder()
                    .reportType("GET_SALES_AND_TRAFFIC_REPORT")
                    .marketplaceIds(List.of(shop.getCountry().getMarketplaceId()))
                    .reportOptions(options)
                    .build();
            var reportString = amazonService.spClient.createAndGetReport(shop.getSeller(), vo, "gzip");
            var report = JsonUtils.parseObject(reportString, JSONObject.class);
            return report;
        })
        .forEach(report -> {
            OssData data = OssData.builder()
                .database(DATABASE)
                .tableName("asin_report")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(dataDate)
                .content(report)
                .headers(null)
                .build();
            service.send(data);
        });
        return "data upload success";
    }
}