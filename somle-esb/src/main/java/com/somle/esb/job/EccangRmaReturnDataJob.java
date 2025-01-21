package com.somle.esb.job;

import com.somle.eccang.model.req.EccangRmaReturnReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

//ram管理——退件列表
@Component
public class EccangRmaReturnDataJob extends EccangDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);


        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
        AtomicInteger totalPages = new AtomicInteger();  // 追踪处理的总页数
        AtomicInteger totalCount = new AtomicInteger();  // 追踪处理的总数
        eccangService.getRmaReturnList(EccangRmaReturnReqVO
            .builder()
            .completeDateStart(beforeYesterdayFirstSecond.format(pattern))
            .completeDateEnd(beforeYesterdayLastSecond.format(pattern))
            .build()
        ).forEach(page -> {
            totalPages.getAndIncrement();  // 增加页面计数
            totalCount.getAndUpdate(v -> page.getTotal());  // 增加总记录数

            OssData data = OssData.builder()
                .database(DATABASE)
                .tableName("rma_return")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(beforeYesterday)
                .content(page)
                .headers(null)
                .build();
            service.send(data);
        });
        // 返回包含总页数和总记录数的结果
        return String.format("Data upload success, total pages processed: %d, total records processed: %d", totalPages.get(), totalCount.get());
    }
}
