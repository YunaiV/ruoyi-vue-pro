package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.csv.TsvUtils;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO;
import com.somle.amazon.controller.vo.AmazonSpReportReqVO.ProcessingStatuses;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmazonspFBMReturnReportDataJob extends AmazonspDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = AmazonSpReportReqVO.builder()
                .reportTypes(List.of("GET_FLAT_FILE_RETURNS_DATA_BY_RETURN_DATE"))
                .processingStatuses(List.of(ProcessingStatuses.DONE))
                .createdSince(beforeYesterdayFirstSecond)
                .createdUntil(beforeYesterdayLastSecond)
                .pageSize(100)
                .build();

        amazonSpService.clients.stream()
            .flatMap(client ->
                client.getReportStream(vo)
            )
            .forEach(report -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("fbm_return_report")
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