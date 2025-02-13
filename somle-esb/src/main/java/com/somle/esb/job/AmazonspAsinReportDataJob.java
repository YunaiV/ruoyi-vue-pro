package com.somle.esb.job;


import cn.hutool.core.text.csv.CsvData;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.amazon.service.AmazonService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.framework.common.util.collection.MapUtils;
import com.somle.framework.common.util.csv.CsvUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.common.util.string.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AmazonspAsinReportDataJob extends AmazonspDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var dataDate = beforeYesterday;

        for (var client : amazonSpService.clients) {
            client.getMarketplaceParticipations().stream()
                .forEach(participation -> {
                    var options = AmazonSpReportSaveVO.ReportOptions.builder()
                        .asinGranularity("CHILD")
                        .dateGranularity("DAY")
                        .build();
                    var vo = AmazonSpReportSaveVO.builder()
                        .reportType("GET_SALES_AND_TRAFFIC_REPORT")
                        .marketplaceIds(List.of(participation.getMarketplace().getId()))
                        .reportOptions(options)
                        .build();
                    var report = client.createAndGetReport(vo);

                    OssData data = OssData.builder()
                        .database(DATABASE)
                        .tableName("asin_report")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(dataDate)
                        .content(JsonUtils.toJSONObject(report))
                        .headers(null)
                        .build();
                    service.send(data);
                });
        }
        return "data upload success";
    }
}