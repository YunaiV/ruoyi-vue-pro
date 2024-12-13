package com.somle.esb.job;

import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

/**
 * @author: Wqh
 * @date: 2024/12/13 14:33
 */
@Component
public class BestbuyOrdersDataJob extends BestbuyDataJob{
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("orders")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(today)
                        .content(bestbuyService.getOrders())
                        .headers(null)
                        .build()
        );

        return "data upload success";
    }
}
