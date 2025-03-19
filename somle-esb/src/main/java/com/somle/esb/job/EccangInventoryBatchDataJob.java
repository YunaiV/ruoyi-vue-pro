package com.somle.esb.job;

import com.somle.eccang.model.req.EccangInventoryBatchReqVO;
import com.somle.esb.model.OssData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

//获取批次库存
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EccangInventoryBatchDataJob extends EccangDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        AtomicInteger totalPages = new AtomicInteger();  // 追踪处理的总页数
        AtomicInteger totalCount = new AtomicInteger();  // 追踪处理的总数
        eccangService.getInventoryBatch(EccangInventoryBatchReqVO.builder()
            .ibUpdateTimeFrom(beforeYesterday)
            .ibUpdateTimeTo(yesterday)
            .build()
        ).forEach(page -> {
            totalPages.getAndIncrement();  // 增加页面计数
            totalCount.getAndUpdate(v -> page.getTotal());  // 增加总记录数

            OssData data = OssData.builder()
                .database(DATABASE)
                .tableName("inventory_batch")
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
