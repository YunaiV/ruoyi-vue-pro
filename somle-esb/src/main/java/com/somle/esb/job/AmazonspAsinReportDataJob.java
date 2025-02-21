package com.somle.esb.job;



import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.esb.model.OssData;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import org.springframework.stereotype.Component;

import java.util.List;

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
                        .content(JsonUtilsX.toJSONObject(report))
                        .headers(null)
                        .build();
                    service.send(data);
                });
        }
        return "data upload success";
    }
}