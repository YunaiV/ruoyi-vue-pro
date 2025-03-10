package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import com.somle.esb.model.OssData;
import com.somle.wangdian.model.WangdianTradeReqVO;
import org.springframework.stereotype.Component;

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

            StreamX.iterate(
                wangdianService.client.execute("trade_query.php", reqVO),
                response -> !response.getInteger("total_count").equals(0),
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