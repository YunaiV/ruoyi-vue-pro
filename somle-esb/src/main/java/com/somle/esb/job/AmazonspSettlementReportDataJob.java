package com.somle.esb.job;


import cn.hutool.json.JSONUtil;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;

import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspSettlementReportDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = AmazonSpReportReqVO.builder()
                .reportTypes(List.of("GET_V2_SETTLEMENT_REPORT_DATA_FLAT_FILE"))
                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .createdSince(beforeYesterdayFirstSecond)
                .createdUntil(beforeYesterdayLastSecond)
                .pageSize(100)
                .build();

        amazonService.account.getSellers().stream()
            .flatMap(seller ->
                amazonService.spClient.getReportStream(seller, vo, null)
            )
            .forEach(report -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("settlement_report")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(JSONUtil.parseArray(report))
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}