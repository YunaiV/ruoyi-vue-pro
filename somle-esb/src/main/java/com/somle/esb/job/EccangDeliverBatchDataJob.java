package com.somle.esb.job;

import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.model.req.EccangDeliverBatchReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: gumaomao
 * @date: 2025/04/09  查询FBA发货管理数据(待发货)上传到易仓
 */
@Component
public class EccangDeliverBatchDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        List<EccangResponse.EccangPage> allPages = new ArrayList<>();
        //获取一天的数据，一天中每次循环获取三分钟的数据
        while (beforeYesterdayFirstSecond.isBefore(beforeYesterdayLastSecond)) {
            LocalDateTime beforeYesterdayFirstSecondPlus = beforeYesterdayFirstSecond.plusMinutes(3);
            var vo = EccangDeliverBatchReqVO.builder()
                .shipStart(beforeYesterdayFirstSecond)
                .shipEnd(beforeYesterdayFirstSecondPlus)
                .pageSize(20)
                .page(1)
                .build();
            List<EccangResponse.EccangPage> pageList = eccangService.streamDeliverBatch(vo).filter(page -> page.getTotal() > 0).toList();
            allPages.addAll(pageList);
            beforeYesterdayFirstSecond = beforeYesterdayFirstSecondPlus;
            //停2秒防止限流
            Thread.sleep(2000);
        }

        allPages.forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("deliver_batch")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            }
        );
        return "data upload success";
    }
}
