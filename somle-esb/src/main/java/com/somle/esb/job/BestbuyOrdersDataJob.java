package com.somle.esb.job;

import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author: Wqh
 * @date: 2024/12/13 14:33
 */
@Component
public class BestbuyOrdersDataJob extends BestbuyDataJob{
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        LocalDate baseDate = LocalDate.parse(param);
        // 2. 计算前两天的日期
        LocalDate targetDate = baseDate.minusDays(2);
        // 3. 生成起始时间（00:00:00）
        LocalDateTime startTime = targetDate.atStartOfDay();
        // 4. 生成结束时间（23:59:59）
        LocalDateTime endTime = targetDate.atTime(23, 59, 59);

        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("orders")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(today)
                    .content(bestbuyService.bestbuyClients.get(0).getAllOrders(startTime, endTime))
                        .headers(null)
                        .build()
        );

        return "data upload success";
    }
}
