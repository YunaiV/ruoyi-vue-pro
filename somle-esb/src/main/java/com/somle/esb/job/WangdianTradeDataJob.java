package com.somle.esb.job;


import com.somle.esb.model.OssData;
import com.somle.framework.common.util.date.LocalDateTimeUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class WangdianTradeDataJob extends WangdianDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTimeUtils.splitIntoHourlyBlocks(yesterdayFirstSecond, yesterdayLastSecond).entrySet().stream().forEach(entry -> {
            Map<String, String> params = new HashMap<String, String>();
            params.put("start_time", entry.getKey().format(formatter));
            params.put("end_time", entry.getValue().format(formatter));
            params.put("page_size", "50");
            params.put("page_no", "0");

            var responseString = wangdianService.client.execute("trade_query.php", params);
            var result = JsonUtils.parseObject(responseString, JSONObject.class);

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
        });



        return "data upload success";
    }
}