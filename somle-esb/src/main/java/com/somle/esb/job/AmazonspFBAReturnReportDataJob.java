package com.somle.esb.job;

import cn.iocoder.yudao.framework.common.util.csv.TsvUtils;
import com.somle.amazon.controller.vo.AmazonSpReportSaveVO;
import com.somle.esb.model.OssData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AmazonspFBAReturnReportDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        AmazonSpReportSaveVO vo = AmazonSpReportSaveVO.builder()
                .reportType("GET_FBA_FULFILLMENT_CUSTOMER_RETURNS_DATA")
                .dataStartTime(beforeYesterdayFirstSecond.toString())
                .dataEndTime(beforeYesterdayLastSecond.toString())
                .build();

        amazonSpService.clients.stream()
            .flatMap(client ->
                client.getMarketplaceParticipations().stream()
                    .map(marketplaceParticipation -> {
                        vo.setMarketplaceIds(List.of(marketplaceParticipation.getMarketplace().getId()));
                        return client.createAndGetReport(vo);
                    })
            )
            .forEach(report -> {

                OssData data = OssData.builder()
                        .database(DATABASE)
                        .tableName("fba_return_report")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(beforeYesterday)
                        .content(TsvUtils.toMapList(report))
                        .headers(getHeaders(vo))
                        .build();

                service.send(data);
            });

        return "data upload success";
    }

    private static Map<String, String> getHeaders(AmazonSpReportSaveVO vo) {
        Map<String, String> headers = new HashMap<>();
        headers.put("gReportType", vo.getReportType());
        headers.put("marketplaceIds", vo.getMarketplaceIds().toString());
        headers.put("dataStartTime", vo.getDataStartTime());
        headers.put("dataEndTime", vo.getDataEndTime());
        return headers;
    }
}
