package com.somle.esb.job;


import com.somle.eccang.model.EccangStockCheckBatchLogVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class EccangStockCheckBatchLogDataJob extends EccangDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        EccangStockCheckBatchLogVO eccangStockCheckBatchLogVO = new EccangStockCheckBatchLogVO();
        eccangStockCheckBatchLogVO.setDateFrom(beforeYesterdayFirstSecond);
        eccangStockCheckBatchLogVO.setDateTo(beforeYesterdayLastSecond);
        eccangService.getStockCheckBatchLog(eccangStockCheckBatchLogVO)
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("stockCheck_batch_log")
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