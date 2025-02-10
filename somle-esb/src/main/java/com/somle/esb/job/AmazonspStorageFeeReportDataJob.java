package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.esb.model.OssData;
import com.somle.framework.common.util.csv.TsvUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspStorageFeeReportDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = AmazonSpReportSaveVO.builder()
                .reportType("GET_FBA_STORAGE_FEE_CHARGES_DATA")
                .dataStartTime(beforeYesterdayFirstSecond.toString())
                .dataEndTime(beforeYesterdayLastSecond.toString())
                .build();

        amazonSpService.clients.stream()
            .map(client ->
                client.createAndGetReportOrNull(vo, "gzip")
            )
            .forEach(report -> {
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
        return "data upload success";
    }
}