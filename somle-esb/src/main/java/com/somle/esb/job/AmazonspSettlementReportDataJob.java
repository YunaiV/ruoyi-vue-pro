package com.somle.esb.job;


import cn.hutool.json.JSONUtil;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;

import com.somle.esb.model.OssData;
import com.somle.framework.common.util.collection.MapUtils;
import com.somle.framework.common.util.csv.TsvUtils;
import com.somle.framework.common.util.json.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        amazonSpService.clients.stream()
            .flatMap(client ->
                client.getReportStream(vo, null)
            )
            .forEach(report -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("settlement_report")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(TsvUtils.toMapList(report))
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}