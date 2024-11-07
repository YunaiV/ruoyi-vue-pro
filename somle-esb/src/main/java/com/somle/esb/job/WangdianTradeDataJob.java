package com.somle.esb.job;


import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WangdianTradeDataJob extends WangdianDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        Map<String, String> params = new HashMap<String, String>();
        params.put("start_time", yesterdayFirstSecond.toString());
        params.put("end_time", yesterdayLastSecond.toString());
        params.put("page_size", "30");
        params.put("page_no", "0");

        var result = wangdianService.client.execute("trade_query.php", params);

        var data = OssData.builder()
                .database(DATABASE)
                .tableName("trade")
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