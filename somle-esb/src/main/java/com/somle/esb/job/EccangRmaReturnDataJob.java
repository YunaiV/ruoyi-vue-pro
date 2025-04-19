package com.somle.esb.job;

import com.somle.eccang.model.req.EccangRmaReturnReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

//ram管理——退件列表
@Component
public class EccangRmaReturnDataJob extends EccangDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = EccangRmaReturnReqVO.builder()
            .page(1)
            .pageSize(100)
            .completeDateStart(beforeYesterdayFirstSecond)
            .completeDateEnd(beforeYesterdayLastSecond)
            .build();
   
        eccangService.getRmaReturnList(vo).forEach(page -> {
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
        return "Data upload success";
    }
}
