package com.somle.esb.job;


import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspStorageFeeReportDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = AmazonSpReportReqVO.builder()
                .reportTypes(List.of("GET_FBA_STORAGE_FEE_CHARGES_DATA"))
//                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .createdSince(beforeYesterdayFirstSecond)
                .createdUntil(beforeYesterdayLastSecond)
                .build();

        amazonSpService.clients.stream()
            .flatMap(client ->
                client.getReportStream(vo, null)
            )
            .forEach(report -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("storage_fee_report")
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