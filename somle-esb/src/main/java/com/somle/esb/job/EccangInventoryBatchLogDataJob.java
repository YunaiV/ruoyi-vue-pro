package com.somle.esb.job;


import com.somle.eccang.model.EccangInventoryBatchLogVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class EccangInventoryBatchLogDataJob extends EccangDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        EccangInventoryBatchLogVO eccangInventoryBatchLogVO = new EccangInventoryBatchLogVO();
        eccangInventoryBatchLogVO.setDateFrom(beforeYesterdayFirstSecond);
        eccangInventoryBatchLogVO.setDateTo(beforeYesterdayLastSecond);
        eccangService.getInventoryBatchLog(eccangInventoryBatchLogVO)
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("inventory_batch_log")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        return "data upload success";
    }
}