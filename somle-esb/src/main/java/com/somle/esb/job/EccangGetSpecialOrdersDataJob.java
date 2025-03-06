package com.somle.esb.job;


import com.somle.eccang.model.req.EccangSpecialOrdersReqVo;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @className: EccangGetSpecialOrdersDataJob
 * @author: gumaomao
 * @date: 2025/03/03
 * @Version: 1.0
 * @description: 易仓WMS获取退件列表
 */
@Component
public class EccangGetSpecialOrdersDataJob extends EccangDataJob{

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        AtomicInteger totalPages = new AtomicInteger();  // 追踪处理的总页数
        AtomicInteger totalCount = new AtomicInteger();  // 追踪处理的总数
        eccangWMSService.getSpecialOrdersList(EccangSpecialOrdersReqVo.builder()
                .spoAddTimeFrom(beforeYesterdayFirstSecond)
                .spoAddTimeTo(beforeYesterdayLastSecond)
                .build()).forEach(
                page -> {
                    totalPages.getAndIncrement();  // 增加页面计数
                    totalCount.getAndUpdate(v -> page.getTotal());  // 增加总记录数

                    OssData data = OssData.builder()
                            .database(DATABASE)
                            .tableName("special_orders")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(today)
                            .content(page)
                            .headers(null)
                            .build();
                    service.send(data);
                }
        );
        // 返回包含总页数和总记录数的结果
        return String.format("Data upload success, total pages processed: %d, total records processed: %d", totalPages.get(), totalCount.get());
    }
}
