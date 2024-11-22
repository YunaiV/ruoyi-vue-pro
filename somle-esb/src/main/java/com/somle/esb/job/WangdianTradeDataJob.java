package com.somle.esb.job;


import com.somle.esb.model.OssData;
import com.somle.framework.common.util.collection.PageUtils;
import com.somle.framework.common.util.date.LocalDateTimeUtils;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.wangdian.model.WangdianTradeReqVO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class WangdianTradeDataJob extends WangdianDataJob{


    @Override

    public String execute(String param) throws Exception {
        setDate(param);

        LocalDateTimeUtils.splitIntoHourlyBlocks(yesterdayFirstSecond, yesterdayLastSecond).entrySet().stream().forEach(entry -> {
            var reqVO = WangdianTradeReqVO.builder()
                .startTime(entry.getKey())
                .endTime(entry.getValue())
                .pageSize(100)
                .pageNo(0)
                .build();

            PageUtils.getAllPages(
                wangdianService.client.execute("trade_query.php", reqVO),
                response -> response.getInteger("total_count").equals(0),
                respoonse ->{
                    reqVO.setPageNo(reqVO.getPageNo() + 1);
                    return wangdianService.client.execute("trade_query.php", reqVO);
                }
            ).forEach(page ->{
                var data = OssData.builder()
                    .database(DATABASE)
                    .tableName("trade")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });


        });



        return "data upload success";
    }
}