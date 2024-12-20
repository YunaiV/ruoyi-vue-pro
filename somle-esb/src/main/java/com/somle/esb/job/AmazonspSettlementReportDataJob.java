package com.somle.esb.job;


import cn.hutool.json.JSONUtil;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;

import com.somle.esb.model.OssData;
import com.somle.framework.common.util.collection.MapUtils;
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

        amazonService.account.getSellers().stream()
            .flatMap(seller ->
                amazonService.spClient.getReportStream(seller, vo, null)
            )
            .forEach(report -> {
                List<Map<String, String>> result = new ArrayList<>();
                for (Map<String, String> map : report){
                    result.add(MapUtils.keyConvertToCamelCase(map));
                }
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("settlement_report")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(JsonUtils.toJSONObject(result))
                    .headers(null)
                    .build();
                service.send(data);
            });
        return "data upload success";
    }
}