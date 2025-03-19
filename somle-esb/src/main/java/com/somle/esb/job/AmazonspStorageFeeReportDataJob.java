package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.csv.TsvUtils;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspStorageFeeReportDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        for (var client : amazonSpService.clients) {
            client.getMarketplaceParticipations().stream()
                .forEach(marketplaceParticipation -> {
                    var vo = AmazonSpReportSaveVO.builder()
                        .reportType("GET_FBA_STORAGE_FEE_CHARGES_DATA")
                        .marketplaceIds(List.of(marketplaceParticipation.getMarketplace().getId()))
                        .dataStartTime(beforeYesterdayFirstSecond.toString())
                        .dataEndTime(beforeYesterdayLastSecond.toString())
                        .build();
                    var report = client.createAndGetReportOrNull(vo);
                    OssData data = OssData.builder()
                        .database(DATABASE)
                        .tableName("storage_fee_report")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(beforeYesterday)
                        .content(report != null ? TsvUtils.toMapList(report) : "")
                        .headers(null)
                        .build();
                    service.send(data);
                });

        }
        return "data upload success";

    }
}