package com.somle.esb.job;


import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class WalmartReconReportDataJob extends WalmartDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var result = walmartService.walmartClients.get(0).getReconFile(yesterday);
        var data = OssData.builder()
            .database(DATABASE)
            .tableName("recon_report")
            .syncType("inc")
            .requestTimestamp(System.currentTimeMillis())
            .folderDate(yesterday)
            .content(result)
            .headers(null)
            .build();
        service.send(data);

        return "data upload success";
    }
}